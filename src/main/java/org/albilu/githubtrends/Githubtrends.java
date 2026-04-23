package org.albilu.githubtrends;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.fusesource.jansi.Ansi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class Githubtrends {

    // TODO batch mode
    //
    public enum Output {
        email,
        console,
        file
    }

    @Option(name = "-t", aliases = "--trends", usage = "daily,weekly,monthly")
    static String[] TRENDS = {
            // "daily",
            "weekly",
            "monthly"
    };

    static String GITHUB_URL = "https://github.com/";

    static ProjectRepository repository = new ProjectRepository();

    static List<Project> trendingProjects = new ArrayList<>();

    @Option(name = "-o", aliases = "--output", usage = "Console/Email")
    public String output = "console";

    @Option(name = "-l", aliases = "--languages", usage = "Code languages to scrap")
    public static String[] CUSTOM_LANGUAGES = {
            "java",
            "c++",
            "c",
            "rust",
            "python",
            "javascript",
            "ruby",
            "go",
            "shell",
            "typescript",
            "zig",
            "kotlin"
    };

    public static void main(String[] args) {

        new Githubtrends().doMain(args);

    }

    private boolean doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            anyLanguagesTrends();
            customLanguagesTrends();
            saveTrendingProjects();
            if (output.equals(Output.email.name())) {
                // TODO sendTrendingsByEmail();
            } else if (output.equals(Output.console.name())) {
                printTrendingsToConsole();
            } else if (output.equals(Output.file.name())) {
                // TODO printTrendingsToFile();
            }
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.out.print("args4j");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
        }
        return false;
    }

    private static void anyLanguagesTrends() {
        // Any Languages
        for (String trend : TRENDS) {
            HttpResponse<String> asString = Unirest.get(String.format("%s%s%s",
                    GITHUB_URL, "trending", "?since=" + trend))
                    .asString();

            Document parse = Jsoup.parse(asString.getBody());
            Elements articles = parse.select("article.Box-row");
            computeTrends(articles);
        }
    }

    private static void customLanguagesTrends() {
        // Custom Languages
        for (String trend : TRENDS) {
            for (String language : CUSTOM_LANGUAGES) {

                HttpResponse<String> asString = Unirest
                        .get(String.format("%s%s%s%s", GITHUB_URL, "trending/", language, "?since=" + trend))
                        .asString();

                Document parse = Jsoup.parse(asString.getBody());
                Elements articles = parse.select("article.Box-row");
                computeTrends(articles);
            }
        }
    }

    private static void printTrendingsToConsole() {
        for (Project project : repository.findAllUsersSortByStarsAndTrendStars()) {
            System.out.println(Ansi.ansi().bold().fgBlue().render(project.getProjectName())
                    .reset().render(String
                            .format(" => %s%s", GITHUB_URL, project.getProjectName().substring(1))));
            System.out.println(project.getDescription());
            System.out.println(Ansi.ansi().bold().fgBlack().bg(Ansi.Color.BLACK).render("● ")
                    .reset().render(project.getCodeLanguage()));
            System.out.println(Ansi.ansi().bold().fgBlack().bg(Ansi.Color.RED).render("★ ")
                    .reset().a(project.getStars()));
            System.out.println(Ansi.ansi().bold().fgBlack().bg(Ansi.Color.RED).render("★ ")
                    .reset().a(project.getTrendStarsString()));
            System.out.println("----------------");
            project.setNew(false);
            repository.updateUser(project);

        }

    }

    private static void computeTrends(Elements articles) {
        for (Element article : articles) {
            String projectName = article.select("h2 > a").attr("href");
            String description = article.select("p").text();
            String codeLanguage = article.select("span[itemprop=programmingLanguage]").text();
            String stars = article.selectFirst("div.f6 > a").text().replaceAll("[^0-9]", "");
            String trendsString = article.select("div.f6 > span").last().text();
            String trends = trendsString.replaceAll("[^0-9]", "");

            Project project = Project.builder()
                    .projectName(projectName)
                    .description(description)
                    .codeLanguage(codeLanguage)
                    .stars(NumberUtils.toInt(stars))
                    .trendStars(NumberUtils.toInt(trends))
                    .trendStarsString(trendsString)
                    .isNew(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            trendingProjects.add(project);

        }
    }

    private static void saveTrendingProjects() {

        for (Project trendingProject : trendingProjects) {

            if (!repository.existsByName(trendingProject.getProjectName())) {

                repository.saveProject(trendingProject);

            }
        }
    }
}
