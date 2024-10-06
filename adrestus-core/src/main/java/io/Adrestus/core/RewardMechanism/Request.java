package io.Adrestus.core.RewardMechanism;

import io.Adrestus.IMemoryTreePool;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Request {
    private RequestType requestType;
    private String requestDescription;
    private IMemoryTreePool memoryTreePool;
    private boolean handled;

    public Request(final RequestType requestType, final String requestDescription) {
        this.requestType = Objects.requireNonNull(requestType);
        this.requestDescription = Objects.requireNonNull(requestDescription);
    }

    public Request(final RequestType requestType, final String requestDescription, final IMemoryTreePool memoryTreePool) {
        this.requestType = Objects.requireNonNull(requestType);
        this.requestDescription = Objects.requireNonNull(requestDescription);
        this.memoryTreePool = Objects.requireNonNull(memoryTreePool);
    }

    public void markHandled() {
        this.handled = true;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return handled == request.handled && requestType == request.requestType && Objects.equals(requestDescription, request.requestDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestType, requestDescription, handled);
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestType=" + requestType +
                ", requestDescription='" + requestDescription + '\'' +
                ", handled=" + handled +
                '}';
    }
}
