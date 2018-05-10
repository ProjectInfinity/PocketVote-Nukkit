package io.pocketvote.task;

import cn.nukkit.scheduler.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.util.ToolBox;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ApiRequest extends AsyncTask {

    private String url;
    private String method;
    private String action;
    private HashMap<String, Object> postFields;
    private String identity;
    private String secret;
    private String version;

    public ApiRequest(String url, String method, String action, HashMap<String, Object> postFields) {
        this.url = url;
        this.method = method;
        this.action = action;
        this.postFields = postFields;
        this.identity = PocketVote.getPlugin().identity;
        this.secret = PocketVote.getPlugin().secret;
        this.version = PocketVote.getPlugin().getDescription().getVersion();
    }

    @Override
    public void onRun() {
        HttpURLConnection con;
        try {
            URL obj = new URL(url);
            if(url.startsWith("https://")) {
                con = (HttpsURLConnection) obj.openConnection();
            } else {
                con = (HttpURLConnection) obj.openConnection();
            }

            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", "PocketVote Nukkit v" + version);
            con.setRequestProperty("Identity", identity);

            if(method.equalsIgnoreCase("POST")) {
                con.setDoOutput(true);
                System.out.println(postFields.get("token"));
                byte[] postData = ToolBox.mapToPostString(postFields).getBytes();
                con.setRequestProperty("Content-Length", Integer.toString(postFields.size()));
                try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(postData);
                }
            }

            int responseCode = con.getResponseCode();

            // If the response code is not 200, assume something went wrong.
            if(responseCode != 200) {
                setResult(createResult(true, "Response code was not ok"));
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.toString());

            // TODO: Remove this line.
            System.out.println(json.toString());

            boolean success = json.hasNonNull("success") && json.get("success").asBoolean();
            TaskResult result = createResult(success);

            if(json.hasNonNull("payload")) result.setRawPayload(json.get("payload"));
            if(json.hasNonNull("meta")) result.setMeta(json.get("meta"));
            if(json.hasNonNull("message")) result.setMessage(json.get("message").asText());

            // The result failed, we do not however know why.
            if(!success) {
                result.setMessage(json.hasNonNull("message") ? json.get("message").asText() : "Unspecified failure.");
                setResult(result);
                return;
            }

            switch(action) {
                case "VOTE":
                    // Only a successful API request can reach this.
                    if(result.hasPayload()) {
                        Jws<Claims> claims = Jwts.parser().setSigningKey(secret.getBytes("UTF-8")).parseClaimsJws(json.get("payload").asText());
                        result.setClaims(claims.getBody(), true);
                    }
                    break;
            }

            setResult(result);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private TaskResult createResult(boolean success) {
        TaskResult result = new TaskResult();
        result.setError(!success);
        return result;
    }

    private TaskResult createResult(boolean success, String message) {
        TaskResult result = new TaskResult();
        result.setError(!success);
        result.setMessage(message);
        return result;
    }
}
