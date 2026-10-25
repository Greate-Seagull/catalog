package com.hungvers.services.catalog.common.cursor;

import java.util.Collection;

public record CursorPage<T>(Collection<T> data, String nextCursor) {
}
