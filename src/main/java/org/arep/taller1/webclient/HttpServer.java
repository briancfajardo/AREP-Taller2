package org.arep.taller1.webclient;

import org.arep.taller1.api.HttpConnection;

import java.net.*;
import java.io.*;
import org.json.*;

/**
 * Clase responsable de crear un Socket entre el cliente y el servidor y entregar las peticiones que el cliente pueda necesitar
 * @author Brian Camilo Fajardo Sanchez
 * @author Daniel Benavides
 */
public class HttpServer {

    /**
     * Método principal que lanza el servidor, acepta y administra la conexión con el cliente y maneja las peticiones del
     * cliente
     * @param args Argumentos necesarios para realizar un método main
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;

        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean firstLine = true;
            String path = null;

            while ((inputLine = in.readLine()) != null) {
                if (firstLine){
                    path = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            outputLine = "HTTP/1.1 200 OK \r\n";


            if (path.startsWith("/movie")){
                outputLine += getMovie(path);
            } else {
                outputLine += getIndex();
            }

            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();

        }

        serverSocket.close();
    }

    /**
     * Método encargado de construir la respuesta de la búsqueda de la información de la película
     * @param path Nombre de la película visto como el path al hacer la petición tipo API REST
     * @return Respuesta con la información de la película
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    private static String getMovie(String path) throws IOException {
        return "Content-Type: text/json \r\n"
                + "\r\n"
                + movieInformation(path);
    }

    /**
     * Método que construye el elemento que se va a pintar en el cliente con la información de la película
     * @param path Título de la película
     * @return Elementos html que contienen la información de la película
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    private static String movieInformation(String path) throws IOException {
        String response = HttpConnection.getMovie(path.split("=")[1]);
        JSONObject object = new JSONObject(response);
        return "<div>" +
                "<h2>"+ object.get("Title") + "</h2>" +
                "<h3> Year: "+ object.get("Year") + "</h3>" +
                "<p> Director: " + object.get("Director") + "</p>" +
                "<p> Genre: " + object.get("Genre") + "</p>" +
                "<p> Rating: " + object.get("Rated") + "</p>" +
                "<p>" + object.get("Plot") + "</p>" +
                "<img src=\"" + object.get("Poster") + "\"/>" +
                "</div>\n";
    }

    /**
     * Método que crea los elementos html de la página inicial (index) que se mostrará en el cliente web
     * @return Estructura de la página del cliente
     */
    private static String getIndex(){
        return "Content-Type: text/html \r\n"
                + "\r\n <!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Movie searcher</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1 style=\"text-align: center;\" >Movie searcher</h1>\n" +
                "        <div style=\"display: flex; justify-content: center;\">"+
                    "        <form id=\"movieForm\" action=\"/movie\">\n" +
                    "            <label for=\"name\">Name of the movie:</label><br>\n" +
                    "            <input style=\"text-align: center;\" type=\"text\" id=\"name\" name=\"name\" value=\"\"><br><br>\n" +
                    "            <input style=\"text-align: center;\" type=\"button\" value=\"Search\" onclick=\"loadGetMsg()\">\n" +
                    "        </form> \n" +
                "        </div>"+
                "        <hr>" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            document.getElementById('movieForm').addEventListener('submit', function(event) {\n" +
                "               event.preventDefault();" +
                "               loadGetMsg()" +
                "            });"+
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/movie?name=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }
}