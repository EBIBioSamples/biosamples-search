# gRPC and Protocol Buffers (Proto) Notes

## Protocol Buffers (Proto)

Protocol Buffers (protobuf) is a language-agnostic serialization format:

1. **Define your data structures** in `.proto` files:

   ```protobuf
   message Sample {
     string id = 1;
     string name = 2;
     int32 count = 3;
   }
   ```

2. **Compile** the `.proto` file using `protoc` to generate code in your target language (Java, Python, Go, etc.)

3. **Serialize/deserialize** data efficiently — more compact and faster than JSON/XML

## gRPC

gRPC is an RPC framework that uses Protocol Buffers by default:

1. **Define services** in `.proto` files:

   ```protobuf
   service SampleService {
     rpc GetSample(SampleRequest) returns (Sample);
     rpc ListSamples(ListRequest) returns (stream Sample);
   }
   ```

2. **Generate client/server code** — `protoc` creates:
   - Server stubs (implement the service methods)
   - Client stubs (call the service methods)

3. **Communication**:
   - Uses HTTP/2 (multiplexing, streaming)
   - Binary protobuf messages (efficient)
   - Strong typing (contract-driven)

## How They Work Together

1. **Design phase**: Write `.proto` files defining messages and services
2. **Code generation**: `protoc` generates language-specific code
3. **Implementation**:
   - **Server**: Implement the generated service interface
   - **Client**: Use the generated client to make calls
4. **Runtime**: gRPC handles serialization, networking, and RPC

## Key Benefits

- **Performance**: Binary format, HTTP/2, efficient serialization
- **Type safety**: Compile-time checks via generated code
- **Streaming**: Supports unary, server streaming, client streaming, bidirectional streaming
- **Language agnostic**: Same `.proto` works across languages
- **Code generation**: Reduces boilerplate

## Example Flow

```
Client                    Server
  |                         |
  |--(protobuf message)---->|
  |                         | (deserializes)
  |                         | (processes)
  |                         | (serializes)
  |<--(protobuf response)---|
  |                         |
```
