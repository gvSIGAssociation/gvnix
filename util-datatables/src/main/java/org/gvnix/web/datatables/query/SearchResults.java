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
 * this program. If not, see <http://www.gnu.org/copyleft/gpl.html>.
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
     * @param result the entities found
     * @param totalResultCount amount of entities found
     * @param isPagedResult true if results are in given page
     * @param startItem page starts at this item index
     * @param pageSize amount of entities in this page
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
     * @return list of entities found
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * @return amount of entities found
     */
    public long getTotalResultCount() {
        return totalResultCount;
    }

    /**
     * @return true if entities found are in given page
     */
    public boolean isPagedResult() {
        return isPagedResult;
    }

    /**
     * @return page starts at this item index
     */
    public long getStartItem() {
        return startItem;
    }

    /**
     * @return amount of entities in this page
     */
    public long getPageSize() {
        return pageSize;
    }

}
