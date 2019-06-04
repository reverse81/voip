package arch3.lge.com.voip.model.user;

public class User {
    private String email;
    private String password;
    private String retypedPassword;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean isEmailValid() {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public boolean isPasswordValid() {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public boolean isSamePassword() {
        //TODO: Replace this with your own logic
        return  password.equals(retypedPassword);
    }

    public String getPassword() {
        return password;
    }


    public String getEmail() {
        return email;
    }

    public void setRetypedPassword(String retypedPassword) {
        this.retypedPassword = retypedPassword;
    }
}
