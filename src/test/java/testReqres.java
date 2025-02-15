import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class testReqres {
    @Test
    public void testGetListUsers(){
        File jsonSchema = new File("src/test/resources/jsonSchema/getListUsersSchema.json");
        RestAssured
                .given()
                .when()
                .get("https://reqres.in/api/users?page=2")
                .then().log().all()
                .assertThat().statusCode(200)
                .assertThat().body("per_page", Matchers.equalTo(6))
                .assertThat().body("page", Matchers.equalTo(2))
                .assertThat().body(JsonSchemaValidator.matchesJsonSchema(jsonSchema));
    }

    @Test
    public void testGetSingleUsers(){
        int userID = 2;
        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get("https://reqres.in/api/users/" + userID)
                .then().log().all()
                .assertThat().statusCode(200)
                .assertThat().body("data.id", Matchers.equalTo(userID));

    }

    @Test
    public void testPostCreateUsers(){
        String valueName = "Sheva";
        String valueJob = "QA";

        JSONObject bodyObj = new JSONObject();
        bodyObj.put("name", valueName);
        bodyObj.put("job", valueJob);

        RestAssured
                .given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(bodyObj.toString())
                .when()
                .post("https://reqres.in/api/users").then().log().all()
                .assertThat().statusCode(201)
                .assertThat().body("name", Matchers.equalTo(valueName));
    }
    @Test
    public void testPatchUpdateUsers(){
        int userID = 271;
        String newName = "Sheva Naufal";
        String newJob = "QA Engineer";

        JSONObject bodyObj = new JSONObject();
        bodyObj.put("name", newName);
        bodyObj.put("job", newJob);

        RestAssured
                .given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(bodyObj.toString())
                .when()
                .patch("https://reqres.in/api/users/2" + userID).then().log().all()
                .assertThat().statusCode(200)
                .assertThat().body("name", Matchers.equalTo(newName));

    }
    @Test
    public void testDeleteUsers(){
        RestAssured
                .given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .delete("https://reqres.in/api/users/2").then().log().all()
                .assertThat().statusCode(204);
    }
    @Test
    public void testUserNotFound(){
        Response response = RestAssured
                .given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get("https://reqres.in/api/users/23").then().log().all()
                .assertThat().statusCode(404)
                .extract().response();

        String responseBody = response.getBody().asString().trim();
        if(responseBody.isEmpty()){
            throw new AssertionError("Test Failed");
        } else {
            System.out.println("Test Passed");
        }
    }
    @Test
    public void testEdgeCase(){
        String baseURL = "https://reqres.in/api/users";
        String longName = "Sheva".repeat(100);
        String requestBody = String.format("{\"name\": \"%s\", \"job\": \"leader\"}", longName);

        Response response = RestAssured
                .given()
                .baseUri(baseURL)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post()
                .then()
                .extract().response();

        int statusCode = response.getStatusCode();

        if(statusCode == 201){
            String name = response.jsonPath().getString("name");
            Assert.assertEquals(name.length(), 500, "Name length should match input length.");
        }else {
            Assert.assertEquals(statusCode, 400, "Name length not match input length.");
        }
    }
}
