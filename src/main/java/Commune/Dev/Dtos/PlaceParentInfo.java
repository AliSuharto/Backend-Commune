package Commune.Dev.Dtos;

public class PlaceParentInfo {
    private String type;
    private Long parentId;
    private String parentNom;

    public PlaceParentInfo(String type, Long parentId, String parentNom) {
        this.type = type;
        this.parentId = parentId;
        this.parentNom = parentNom;
    }

    // Getters
    public String getType() { return type; }
    public Long getParentId() { return parentId; }
    public String getParentNom() { return parentNom; }

    @Override
    public String toString() {
        return type + ": " + parentNom + " (ID: " + parentId + ")";
    }
}
