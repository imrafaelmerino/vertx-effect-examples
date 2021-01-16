package vertx.effect.examples.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.time.Instant;

public class InstantCodec implements MessageCodec<Instant, Instant> {
    public static final InstantCodec INSTANCE = new InstantCodec();

    private InstantCodec(){}

    @Override
    public void encodeToWire(final Buffer buffer,
                             final Instant instant) {
        byte[] bytes = instant.toString()
                              .getBytes();
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public Instant decodeFromWire(int pos,
                                  final Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        return Instant.parse(buffer.getString(pos,
                                              pos + length));
    }

    @Override
    public Instant transform(final Instant instant) {
        return instant;
    }

    @Override
    public String name() {
        return "my-instant-codec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
