package eliasstar.utils.mocks;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public final class HttpClientMock extends HttpClient {

    private boolean shouldSucceed;
    private String id;
    private String requestBody;

    public void setResponse(String id, boolean shouldSucceed) {
        this.id = id;
        this.shouldSucceed = shouldSucceed;
    }

    public String getRequest() {
        return requestBody;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest req, BodyHandler<T> handler) throws IOException, InterruptedException {
        var bodySubscriber = BodySubscribers.ofString(Charset.defaultCharset());
        req.bodyPublisher().get().subscribe(new Flow.Subscriber<ByteBuffer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                bodySubscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(ByteBuffer item) {
                bodySubscriber.onNext(List.of(item));
            }

            @Override
            public void onError(Throwable throwable) {
                bodySubscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                bodySubscriber.onComplete();
            }
        });

        var resBody = shouldSucceed ? "{\"jsonrpc\": \"2.0\",\"id\": \"" + id + "\",\"result\": \"test\"}" : "{\"jsonrpc\": \"2.0\",\"id\": \"" + id + "\",\"error\": {\"code\":42069,\"message\": \"test\"}}";
        var bodyBytes = ByteBuffer.wrap(resBody.getBytes());

        var res = new HttpResponseMock<T>(req);
        var resSubscriber = handler.apply(res);

        var publisher = new SubmissionPublisher<List<ByteBuffer>>();
        publisher.subscribe(resSubscriber);
        publisher.submit(List.of(bodyBytes));
        publisher.close();

        try {
            requestBody = bodySubscriber.getBody().toCompletableFuture().get();
            res.body(resSubscriber.getBody().toCompletableFuture().get());
        } catch (ExecutionException e) {
            throw new IOException(e);
        }

        return res;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler, PushPromiseHandler<T> pushPromiseHandler) {
        return null;
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Version version() {
        return Version.HTTP_1_1;
    }

}