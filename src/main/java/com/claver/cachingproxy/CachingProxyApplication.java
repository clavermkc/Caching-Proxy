package com.claver.cachingproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class CachingProxyApplication {

	public static void main(String[] args) {
		List<String> argsList = Arrays.asList(args);

		if (argsList.contains("--clear-cache")) {
			executeClearCache(argsList);
			return; // On arrête ici, on ne lance pas le serveur
		}

		// Traduction des arguments pour Spring Boot
		List<String> springArgs = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			if ("--port".equals(args[i]) && i + 1 < args.length) {
				springArgs.add("--server.port=" + args[i + 1]);
				i++; // On saute la valeur
			} else if ("--origin".equals(args[i]) && i + 1 < args.length) {
				springArgs.add("--proxy.origin=" + args[i + 1]);
				i++; // On saute la valeur
			} else {
				springArgs.add(args[i]);
			}
		}

		SpringApplication.run(CachingProxyApplication.class, springArgs.toArray(new String[0]));
	}

	private static void executeClearCache(List<String> argsList) {
		// Par défaut, on va chercher le port 8082 comme dans ton application.properties
		// Sauf si l'utilisateur spécifie un port avec --port lors du clear-cache
		String port = "8082";
		for (int i = 0; i < argsList.size(); i++) {
			if ("--port".equals(argsList.get(i)) && i + 1 < argsList.size()) {
				port = argsList.get(i + 1);
			}
		}

		try {
			//Todo later use Restclient
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:" + port + "/internal/clear-cache"))
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				System.out.println(" Cache vidé avec succès !");
			} else {
				System.out.println("Erreur lors du vidage du cache. Le serveur est-il lancé sur le port " + port + " ?");
			}
		} catch (Exception e) {
			System.out.println(" Impossible de contacter le serveur. Assure-toi qu'il tourne sur le port " + port);
		}
	}
}