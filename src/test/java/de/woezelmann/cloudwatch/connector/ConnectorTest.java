package de.woezelmann.cloudwatch.connector;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import org.junit.Test;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectorTest {


    @Test
    public void fetchSimpleMetrics() throws Exception {

        AmazonCloudWatchClientBuilder standard = AmazonCloudWatchClientBuilder.standard();
        standard.setCredentials(new StaticCredentialsProvider(new BasicSessionCredentials(
                "xxx",
                "yyy",
                "zzz"
        )));

        AmazonCloudWatch amazonCloudWatch = standard.build();


        ListMetricsResult listMetricsResult = amazonCloudWatch.listMetrics(
                new ListMetricsRequest()
                        .withNamespace("AWS/ELB")
//                        .withMetricName("RequestCount")
        );

        System.out.println(listMetricsResult);

        GetMetricStatisticsResult metricStatistics = amazonCloudWatch.getMetricStatistics(
                new GetMetricStatisticsRequest()
                        .withNamespace("AWS/ELB")
                        .withMetricName("Latency")
                        .withDimensions(
                                new Dimension()
                                        .withName("LoadBalancerName")
                                        .withValue("proxy-green-154")
                        )
                        .withStatistics(Statistic.values())
                        .withStartTime(new Date(1517382000000L))
                        .withEndTime(new Date(1517408725000L))
                        .withPeriod(60)
        );

        System.out.println(metricStatistics);

        List<Datapoint> sorted = metricStatistics.getDatapoints()
                .stream()
                .sorted(Comparator.comparing(Datapoint::getTimestamp))
                .collect(Collectors.toList());


        sorted.forEach(System.out::println);

        XYChart chart = QuickChart.getChart(
                "Cloud Watch example",
                "time"
                , "latency",
                "blub",
                sorted.stream().map(datapoint -> datapoint.getTimestamp().getTime()).collect(Collectors.toList()),
                sorted.stream().map(datapoint -> datapoint.getAverage()).collect(Collectors.toList())
        );

        new SwingWrapper(chart).displayChart();

        Thread.sleep(100000L);
    }
}