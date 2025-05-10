package model;


public abstract class User {
    private String userName;
    private String password;
    private String legalName;
    private String type;

    public User(String userName, String password, String legalName, String type) {
        this.userName = userName;
        this.password = password;
        this.legalName = legalName;
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

}