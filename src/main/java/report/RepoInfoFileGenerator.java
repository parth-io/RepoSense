package report;

import analyzer.RepoAnalyzer;
import dataObject.FileInfo;
import dataObject.RepoConfiguration;
import dataObject.RepoContributionSummary;
import dataObject.RepoInfo;
import git.GitCloner;
import util.Constants;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by matanghao1 on 8/7/17.
 */
public class RepoInfoFileGenerator {

    public static void generateReposReport(List<RepoConfiguration> repoConfigs, String targetFileLocation){
        String reportName = generateReportName();
        List<RepoInfo> repos = analyzeRepos(repoConfigs);
        copyStaticLib(reportName, targetFileLocation);

        for (RepoInfo repo : repos) {
            generateIndividualRepoReport(repo, reportName,targetFileLocation);
        }

        Map<String, RepoContributionSummary> repoSummaries = ContributionSummaryGenerator.analyzeContribution(repos);
        FileUtil.writeJSONFile(repoSummaries, getSummaryResultPath(reportName,targetFileLocation), "summaryJson");
        FileUtil.copyFile(new File(Constants.STATIC_SUMMARY_REPORT_FILE_ADDRESS),new File(getSummaryPagePath(reportName,targetFileLocation)));

    }

    private static List<RepoInfo> analyzeRepos(List<RepoConfiguration> configs) {
        List<RepoInfo> result = new ArrayList<>();
        int count = 1;
        for (RepoConfiguration config : configs) {
            System.out.println("Analyzing Repository No."+(count++)+"( " + configs.size() + " repositories in total)");
            GitCloner.downloadRepo(config.getOrganization(), config.getRepoName(), config.getBranch());
            RepoInfo repoinfo = new RepoInfo(config.getOrganization(), config.getRepoName(),config.getBranch(),config.getAuthorDisplayNameMap());
            RepoAnalyzer.analyzeCommits(config, repoinfo);
            result.add(repoinfo);
        }
        return result;
    }

    private static void generateIndividualRepoReport(RepoInfo repoinfo, String reportName, String targetFileLocation){

        String repoReportName = repoinfo.getDirectoryName();
        String repoReportDirectory = targetFileLocation+"/"+reportName+"/"+repoReportName;
        new File(repoReportDirectory).mkdirs();
        copyTemplate(repoReportDirectory, Constants.STATIC_INDIVIDUAL_REPORT_TEMPLATE_ADDRESS);
        ArrayList<FileInfo> fileInfos = repoinfo.getCommits().get(repoinfo.getCommits().size()-1).getFileinfos();
        FileUtil.writeJSONFile(fileInfos,getIndividualResultPath(repoReportDirectory),"resultJson");

        System.out.println("report for "+ repoReportName+" Generated!");

    }

    private static void copyStaticLib(String reportName, String targetFileLocation){
        String staticLibDirectory = targetFileLocation +"/"+reportName+"/"+"static";
        new File(staticLibDirectory).mkdirs();
        copyTemplate(staticLibDirectory, Constants.STATIC_LIB_TEMPLATE_ADDRESS );
    }


    private static void copyTemplate(String dest, String src){
        try {
            FileUtil.copyFiles(new File(src), new File(dest));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getSummaryPagePath(String repoReportDirectory, String targetFileLocation){
        return targetFileLocation + "/"+repoReportDirectory+ "/index.html";
    }

    private static String getDetailPagePath(String repoReportDirectory, String targetFileLocation){
        return targetFileLocation+"/"+repoReportDirectory+ "/detail.html";
    }

    private static String getIndividualResultPath(String repoReportDirectory){
        return repoReportDirectory+ "/result.js";
    }

    private static String getSummaryResultPath(String reportName, String targetFileLocation){
        return targetFileLocation+"/"+reportName+"/summary.js";
    }

    private static String generateReportName(){
        return Constants.REPORT_NAME_FORMAT.format(new Date());
    }

}