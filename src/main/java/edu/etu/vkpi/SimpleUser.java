package edu.etu.vkpi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by Denis on 03.01.2017.
 */
public class SimpleUser {
    private Integer id;
    private Integer sex;
    private String firstName;
    private String lastName;
    private Integer relation;
    private String bdate;
    private Integer age;
    private Integer cityId;
    private String cityTitle;
    private String domain;

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", first='").append(firstName).append("'");
        sb.append(", last='").append(lastName).append("'");
        sb.append(", sex='").append(sex).append("'");
        sb.append(", relation='").append(relation).append("'");
        sb.append(", bdate='").append(bdate).append("'");
        sb.append(", city='").append(cityTitle).append("'");
        sb.append(", domain=vk.com/").append(domain);
        sb.append('}');
        return sb.toString();
    }

    public String format(){
        final StringBuilder sb = new StringBuilder("");
        sb.append("'").append(cityTitle).append("'");
        sb.append(" '").append(sex == 0 ? "?" : (sex == 1 ? "f" : "m")).append("'");
        sb.append(" '").append(relation != null ? relation : "?").append("'");
        sb.append(" '").append(age != null ? age : "??").append("'");
        sb.append(" https://vk.com/").append(domain);
        sb.append(" '").append(firstName).append(" ").append(lastName).append("'");
        sb.append("");
        return sb.toString();
    }

    public boolean calculateAge(){
        age = null;
        if(bdate != null && bdate != null && bdate.length() > 6){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.y");
            try {
                LocalDate birthday = LocalDate.parse(bdate, formatter);
                LocalDate now = LocalDate.now();
                long years = ChronoUnit.YEARS.between(birthday, now);
                age = (int) years;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getBdate() {
        return bdate;
    }

    public void setBdate(String bdate) {
        this.bdate = bdate;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCityTitle() {
        return cityTitle;
    }

    public void setCityTitle(String cityTitle) {
        this.cityTitle = cityTitle;
    }

    public Integer getRelation() {
        return relation;
    }

    public void setRelation(Integer relation) {
        this.relation = relation;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
