package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.ListApiService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListApiServiceImpl implements ListApiService {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void createGoods(Class<Goods> goodsClass) {
        elasticsearchRestTemplate.createIndex(goodsClass);
        elasticsearchRestTemplate.putMapping(goodsClass);
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        Goods goods = new Goods();
        goods.setTitle(skuInfo.getSkuName());
        goods.setHotScore(0l);
        goods.setCategory3Id(skuInfo.getCategory3Id());
        goods.setCreateTime(new Date());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTmId(skuInfo.getTmId());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        BaseTrademark baseTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setTmName(baseTrademark.getTmName());
        goods.setId(skuId);
        List<SearchAttr> searchAttrList = productFeignClient.getSearchAttrs(skuId);

        goods.setAttrs(searchAttrList);

        goodsRepository.save(goods);
    }

    @Override
    public void cancelSale(Long skuId) {
        Goods goods = new Goods();
        goods.setId(skuId);
        goodsRepository.delete(goods);
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        // 调用product服务，查询category列表
        List<BaseCategoryView> baseCategoryViews = productFeignClient.getBaseCategoryList();
        // 将category列表转化为List<JSONObject>套娃
        //一级分类集合
        List<JSONObject> jobc1s = new ArrayList<>();
        Map<Long, List<BaseCategoryView>> map1 = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> group1 : map1.entrySet()) {
            //一级分类元素
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("categoryId", group1.getKey());
            jsonObject1.put("categoryName", group1.getValue().get(0).getCategory1Name());

            List<JSONObject> jobc2s = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> map2 = group1.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> group2 : map2.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("categoryId", group2.getKey());
                jsonObject2.put("categoryName", group2.getValue().get(0).getCategory2Name());

                List<JSONObject> jobc3s = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> map3 = group2.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> group3 : map3.entrySet()) {
                    JSONObject jsonObject3 = new JSONObject();
                    jsonObject3.put("categoryId", group3.getKey());
                    jsonObject3.put("categoryName", group3.getValue().get(0).getCategory3Name());

                    //把三级分类元素放到三级分类集合中
                    jobc3s.add(jsonObject3);
                    //把三级分类集合放到二级分类元素中
                    jsonObject2.put("categoryChild", jobc3s);
                }
                //把二级分类元素放到二级分类集合中
                jobc2s.add(jsonObject2);
                //把二级分类集合放到一级分类元素中
                jsonObject1.put("categoryChild", jobc2s);
            }
            //把一级分类元素放到一级分类集合中
            jobc1s.add(jsonObject1);
        }
        return jobc1s;
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //封装dsl请求
        SearchRequest searchRequest = getSearchRequest(searchParam);
        try {
            //执行检索
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析检索结果
            searchResponseVo = getSearchResponse(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVo;
    }

    @Override
    public void hotScore(Long skuId) {
        Long score = 0L;
        score = redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1);
        if (score%10==0){
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(score);
            goodsRepository.save(goods);
        }
    }

    //封装解析检索结果
    private SearchResponseVo getSearchResponse(SearchResponse search) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        List<Goods> goodsList = new ArrayList<>();
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //搜索关键字高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            if(highlightField !=null ){
                String text = highlightField.getFragments()[0].toString();
                goods.setTitle(text);
            }
            goodsList.add(goods);
        }
        searchResponseVo.setGoodsList(goodsList);

        Aggregations aggregations = search.getAggregations();

        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregations.get("tmIdAgg");
        List<SearchResponseTmVo> searchResponseTmVos = tmIdAgg.getBuckets().stream().map(tmIdBucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            // 商标id
            Long tmId = tmIdBucket.getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);

            // 商标名称
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdBucket.getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            // 商标url
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdBucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            return searchResponseTmVo;
        }).collect(Collectors.toList());

        ParsedNested attrsAgg = (ParsedNested) aggregations.get("attrsAgg");//多一层nested
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(attrIdBucket->{
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

            long attrId = attrIdBucket.getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);

            ParsedStringTerms attrNameAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            ParsedStringTerms attrValueAgg = (ParsedStringTerms) attrIdBucket.getAggregations().get("attrValueAgg");
            List<String> valueList = attrValueAgg.getBuckets().stream().map(attrValueBucket->{
                String attrValue = attrValueBucket.getKeyAsString();
                return attrValue;
            }).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueList);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());

//        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
//        for (Terms.Bucket bucket : tmIdAgg.getBuckets()) {
//            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
//            //商标Id
//            long tmId = bucket.getKeyAsNumber().longValue();
//            searchResponseTmVo.setTmId(tmId);
//            //商标名称
//            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
//            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
//            searchResponseTmVo.setTmName(tmName);
//            //商标Url
//            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
//            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
//            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
//
//            searchResponseTmVos.add(searchResponseTmVo);
//        }

        searchResponseVo.setTrademarkList(searchResponseTmVos);
        searchResponseVo.setAttrsList(searchResponseAttrVos);

        return searchResponseVo;
    }

    //封装dsl请求
    private SearchRequest getSearchRequest(SearchParam searchParam) {
        Long category3Id = searchParam.getCategory3Id();// 三级分类id
        String order = searchParam.getOrder();// 排序
        String[] props = searchParam.getProps();// 属性
        String trademark = searchParam.getTrademark();// 商标
        String keyword = searchParam.getKeyword();// 关键字

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");

        // 封装检索dsl
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();// {}

        // query
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (null != category3Id) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (!StringUtils.isEmpty(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
            //搜索关键词高亮
            HighlightBuilder highlightBuilder =new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        if (!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            Long tmId = Long.parseLong(split[0]);
            String tmName = split[1];
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId",tmId);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (props!=null&&props.length>0){
            for (String prop : props) {
                String[] split = prop.split(":");
                Long attrId = Long.valueOf(split[0]);
                String attrName = split[2];
                String attrValue = split[1];

                BoolQueryBuilder boolQueryBuilderNested = new BoolQueryBuilder();

                // nested内部检索结构
                BoolQueryBuilder subBoolQuery = new BoolQueryBuilder();// 建立一个属于nested字段的子bool检索结构
                TermQueryBuilder termQueryAttrId = new TermQueryBuilder("attrs.attrId",attrId);
                subBoolQuery.filter(termQueryAttrId);
                TermQueryBuilder termQueryAttrName = new TermQueryBuilder("attrs.attrName",attrName);
                subBoolQuery.filter(termQueryAttrName);
                TermQueryBuilder termQueryAttrValue = new TermQueryBuilder("attrs.attrValue",attrValue);
                subBoolQuery.filter(termQueryAttrValue);
                // 将内部bool放入nested中
                //boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs",subBoolQuery, ScoreMode.None);
                boolQueryBuilderNested.filter(nestedQueryBuilder);//将nested的检索放入总bool复合检索中
                boolQueryBuilder.filter(boolQueryBuilderNested);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);// 将bool查询放入query

        // dsl分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);

        // aggs
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId").subAggregation(
                AggregationBuilders.terms("tmNameAgg").field("tmName")
        ).subAggregation(
                AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")
        ));// 两层聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg", "attrs").subAggregation(
                AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").subAggregation(
                        AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")
                ).subAggregation(
                        AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")
                )
        ));// 三层聚合

        //sort 排序
        if (StringUtils.isNotEmpty(order)){
            String[] split = order.split(":");

            String orderNum = split[0];
            String orderRule = split[1];

            String orderName = "";
            if (orderNum.equals("1")){
                orderName = "hotScore";
            }else if (orderNum.equals("2")){
                orderName = "price";
            }
            searchSourceBuilder.sort(orderName,orderRule.equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }

        System.out.println(searchSourceBuilder.toString());// 打印一下dsl语句
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }
}
