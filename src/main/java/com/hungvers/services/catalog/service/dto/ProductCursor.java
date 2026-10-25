package com.hungvers.services.catalog.service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductCursor(int rank, OffsetDateTime createdAt, UUID id) {}
