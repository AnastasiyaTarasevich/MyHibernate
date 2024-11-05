package entities;

public class NotEntityClass {
    private Integer id;
    private String name;
   private Integer something;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSomething() {
        return something;
    }

    public void setSomething(Integer something) {
        this.something = something;
    }

    @Override
    public String toString() {
        return "NotEntityClass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", something=" + something +
                '}';
    }
}
