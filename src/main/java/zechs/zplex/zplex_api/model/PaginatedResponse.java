package zechs.zplex.zplex_api.model;

import java.util.List;

public class PaginatedResponse<T> {

    private List<T> data;
    private int pageNumber;
    private int pageCount;

    public PaginatedResponse(List<T> data, int pageNumber, int pageCount) {
        this.data = data;
        this.pageNumber = pageNumber;
        this.pageCount = pageCount;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}