/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.web.datatables.query;

import java.util.List;

/**
 * Data bundle for paged search results
 * 
 * @author gvNIX team
 * @param <T>
 */
public class SearchResults<T> {

    /**
     * Create a bundle of query results.
     * 
     * @param result
     * @param totalResultCount
     * @param isPagedResult
     * @param startItem
     * @param pageSize
     */
    public SearchResults(List<T> result, long totalResultCount,
            boolean isPagedResult, long startItem, long pageSize) {
        super();
        this.results = result;
        this.totalResultCount = totalResultCount;
        this.isPagedResult = isPagedResult;
        this.startItem = startItem;
        this.pageSize = pageSize;
    }

    private final List<T> results;

    private final long totalResultCount;

    private final boolean isPagedResult;

    private final long startItem;

    private final long pageSize;

    /**
     * @return item result list
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * @return total result of criteria
     */
    public long getTotalResultCount() {
        return totalResultCount;
    }

    /**
     * @return if {@link #getResult()} is paged
     */
    public boolean isPagedResult() {
        return isPagedResult;
    }

    /**
     * @return start item index of {@link #getResult()}
     */
    public long getStartItem() {
        return startItem;
    }

    /**
     * @return the page size required
     */
    public long getPageSize() {
        return pageSize;
    }

}
