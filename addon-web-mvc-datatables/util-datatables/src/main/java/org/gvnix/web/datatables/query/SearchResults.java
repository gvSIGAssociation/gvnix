/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.datatables.query;

import java.util.List;

/**
 * Data bundle for paged search results
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @param <T>
 */
public class SearchResults<T> {

    /**
     * Create a bundle of query results.
     * 
     * @param result the entities found
     * @param resultsCount amount of entities found taking in account search and
     *        paging criterias
     * @param isPagedResult true if results are in given page
     * @param startItem page starts at this item index
     * @param pageSize amount of entities in this page
     * @param totalCount total amount of entities without paging
     */
    public SearchResults(List<T> result, long resultsCount,
            boolean isPagedResult, long startItem, long pageSize,
            long totalCount) {
        super();
        this.results = result;
        this.resultsCount = resultsCount;
        this.isPagedResult = isPagedResult;
        this.startItem = startItem;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    private final List<T> results;

    private final long totalCount;

    private final long resultsCount;

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
    public long getResultsCount() {
        return resultsCount;
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

    /**
     * @return total amount of entities without paging
     */
    public long getTotalCount() {
        return totalCount;
    }

}
