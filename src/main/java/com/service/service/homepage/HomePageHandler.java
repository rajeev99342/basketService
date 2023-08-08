package com.service.service.homepage;

import com.service.model.DisplayProductModel;
import com.service.model.HomePageData;
import com.service.model.HomePageModel;

import java.util.List;
import java.util.Map;

public interface HomePageHandler {
    void putHomePageData();
    Map<String, List<DisplayProductModel>> getHomePageData(int pageIndex, int pageSize);
    List<HomePageData> getHomePageDataV2(int pageIndex, int pageSize);
    HomePageModel getHomeDate();
}
