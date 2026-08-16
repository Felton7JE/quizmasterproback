package quizmaster.quiz.auth.DTO;



public class acessDTO {
 
    private String token;

    public String getToken() {
        return token;
    }

    public acessDTO(String token) {
        super();
        this.token = token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}

