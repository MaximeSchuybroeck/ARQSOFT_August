package com.example.lending_service;

import org.junit.platform.suite.api.Suite;
import com.example.lending_service.acceptance.RecommendationSummary;
import com.example.lending_service.acceptance.ReturnRecommendation;
import com.example.lending_service.integration.LendingController;
import com.example.lending_service.mutation.RecommendationSummaryMut;
import com.example.lending_service.opaque.LendingServiceReturn;
import com.example.lending_service.transparent.LendingServiceReturnTrans;
import org.junit.platform.suite.api.SelectClasses;


@Suite
@SelectClasses({
        RecommendationSummary.class,
        ReturnRecommendation.class,
        LendingController.class,
        RecommendationSummaryMut.class,
        LendingServiceReturn.class,
        LendingServiceReturnTrans.class


})

public class AllTestsSuiteLending {
}
