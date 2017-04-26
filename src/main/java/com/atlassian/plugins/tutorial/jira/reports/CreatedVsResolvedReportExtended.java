package com.atlassian.plugins.tutorial.jira.reports;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.report.AbstractChartReport;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class CreatedVsResolvedReportExtended
        extends AbstractChartReport {
    private static final Logger log = LoggerFactory.getLogger(CreatedVsResolvedReportExtended.class);
    private final ChartFactory chartFactory;
    private final TimeZoneManager timeZoneManager;

    @Inject
    public CreatedVsResolvedReportExtended(@ComponentImport JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties, @ComponentImport ProjectManager projectManager, @ComponentImport SearchRequestService searchRequestService, @ComponentImport ChartUtils chartUtils, @ComponentImport ChartFactory chartFactory, @ComponentImport TimeZoneManager timeZoneManager) {
        super(authenticationContext, applicationProperties, projectManager, searchRequestService, chartUtils);
        this.chartFactory = chartFactory;
        this.timeZoneManager = timeZoneManager;
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("report", (Object)this);
        params.put("action", (Object)action);
        params.put("user", (Object)this.authenticationContext.getUser());
        params.put("timePeriods", (Object)new TimePeriodUtils(this.timeZoneManager));
        String projectOrFilterId = (String)reqParams.get("projectOrFilterId");
        ChartFactory.PeriodName periodName = ChartFactory.PeriodName.valueOf((String)((String)reqParams.get("periodName")));
        int days = 30;
        if (reqParams.containsKey("daysprevious")) {
            days = Integer.parseInt((String)reqParams.get("daysprevious"));
        }
        ChartFactory.VersionLabel versionLabel = ChartFactory.VersionLabel.none;
        if (reqParams.containsKey("versionLabels")) {
            versionLabel = ChartFactory.VersionLabel.valueOf((String)((String)reqParams.get("versionLabels")));
        }
        boolean cumulative = false;
        if (reqParams.containsKey("cumulative")) {
            cumulative = ((String)reqParams.get("cumulative")).equalsIgnoreCase("true");
        }
        boolean showUnresolvedTrend = "true".equalsIgnoreCase((String)reqParams.get("showUnresolvedTrend"));
        try {
            SearchRequest request = this.chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
            params.put("projectOrFilterId", projectOrFilterId);
            ChartFactory.ChartContext context = new ChartFactory.ChartContext(this.authenticationContext.getUser(), request, 800, 500, true);
            Chart chart = this.chartFactory.generateCreatedVsResolvedChart(context, days, periodName, versionLabel, cumulative, showUnresolvedTrend);
            params.putAll(chart.getParameters());
        }
        catch (Exception e) {
            log.error("Could not create velocity parameters " + e.getMessage(), (Throwable)e);
        }
        return this.descriptor.getHtml("view", params);
    }
}
