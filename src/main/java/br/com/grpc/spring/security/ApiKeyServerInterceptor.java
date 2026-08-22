package br.com.grpc.spring.security;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exige o header (metadata) "x-api-key" em toda chamada gRPC, comparando com o valor
 * configurado em {@code app.security.api-key} (variável de ambiente {@code API_KEY}).
 * Aplicado globalmente a todos os serviços via {@link GlobalServerInterceptor} — não
 * depende de TLS estar habilitado, mas para proteger a chave em trânsito é recomendado
 * combinar com o perfil "tls" (ver application-tls.properties) fora de um ambiente local.
 */
@Component
@GlobalServerInterceptor
public class ApiKeyServerInterceptor implements ServerInterceptor {

    static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);

    private final String expectedApiKey;

    public ApiKeyServerInterceptor(@Value("${app.security.api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String providedApiKey = headers.get(API_KEY_HEADER);
        if (providedApiKey == null || !constantTimeEquals(providedApiKey, expectedApiKey)) {
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Header 'x-api-key' ausente ou inválido."), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        return next.startCall(call, headers);
    }

    /**
     * Compara em tempo constante para não vazar a chave por análise de tempo de resposta
     * (comparação ingênua de String, byte a byte com short-circuit, é vulnerável a esse tipo
     * de ataque de canal lateral).
     */
    private static boolean constantTimeEquals(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
