/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
