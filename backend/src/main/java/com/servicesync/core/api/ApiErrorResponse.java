package com.servicesync.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ValidationError> details;

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<ValidationError> getDetails() { return details; }
    public void setDetails(List<ValidationError> details) { this.details = details; }

    public static class ValidationError {
        private String field;
        private String issue;

        public ValidationError(String field, String issue) {
            this.field = field;
            this.issue = issue;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getIssue() { return issue; }
        public void setIssue(String issue) { this.issue = issue; }
    }
}
