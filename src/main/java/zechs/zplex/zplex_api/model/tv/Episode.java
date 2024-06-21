package zechs.zplex.zplex_api.model.tv;

public class Episode {

    private Integer id;
    private String title;
    private Integer episodeNumber;
    private String stillPath;
    private String fileId;

    public Episode() {
    }

    public Episode(Integer id, String title, Integer episodeNumber, String stillPath, String fileId) {
        this.id = id;
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.stillPath = stillPath;
        this.fileId = fileId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getStillPath() {
        return stillPath;
    }

    public void setStillPath(String stillPath) {
        this.stillPath = stillPath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}

