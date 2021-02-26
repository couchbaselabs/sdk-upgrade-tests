package com.couchbase.grpc.protocol;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: framework.proto")
public final class UpgradeTestingServiceGrpc {

  private UpgradeTestingServiceGrpc() {}

  public static final String SERVICE_NAME = "protocol.UpgradeTestingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.CreateConnectionRequest,
      com.couchbase.grpc.protocol.CreateConnectionResponse> getCreateConnectionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "createConnection",
      requestType = com.couchbase.grpc.protocol.CreateConnectionRequest.class,
      responseType = com.couchbase.grpc.protocol.CreateConnectionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.CreateConnectionRequest,
      com.couchbase.grpc.protocol.CreateConnectionResponse> getCreateConnectionMethod() {
    io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.CreateConnectionRequest, com.couchbase.grpc.protocol.CreateConnectionResponse> getCreateConnectionMethod;
    if ((getCreateConnectionMethod = UpgradeTestingServiceGrpc.getCreateConnectionMethod) == null) {
      synchronized (UpgradeTestingServiceGrpc.class) {
        if ((getCreateConnectionMethod = UpgradeTestingServiceGrpc.getCreateConnectionMethod) == null) {
          UpgradeTestingServiceGrpc.getCreateConnectionMethod = getCreateConnectionMethod = 
              io.grpc.MethodDescriptor.<com.couchbase.grpc.protocol.CreateConnectionRequest, com.couchbase.grpc.protocol.CreateConnectionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "protocol.UpgradeTestingService", "createConnection"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.couchbase.grpc.protocol.CreateConnectionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.couchbase.grpc.protocol.CreateConnectionResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UpgradeTestingServiceMethodDescriptorSupplier("createConnection"))
                  .build();
          }
        }
     }
     return getCreateConnectionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.UpgradeTestRequest,
      com.couchbase.grpc.protocol.UpgradeTestResponse> getDoUpgradeTestsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "doUpgradeTests",
      requestType = com.couchbase.grpc.protocol.UpgradeTestRequest.class,
      responseType = com.couchbase.grpc.protocol.UpgradeTestResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.UpgradeTestRequest,
      com.couchbase.grpc.protocol.UpgradeTestResponse> getDoUpgradeTestsMethod() {
    io.grpc.MethodDescriptor<com.couchbase.grpc.protocol.UpgradeTestRequest, com.couchbase.grpc.protocol.UpgradeTestResponse> getDoUpgradeTestsMethod;
    if ((getDoUpgradeTestsMethod = UpgradeTestingServiceGrpc.getDoUpgradeTestsMethod) == null) {
      synchronized (UpgradeTestingServiceGrpc.class) {
        if ((getDoUpgradeTestsMethod = UpgradeTestingServiceGrpc.getDoUpgradeTestsMethod) == null) {
          UpgradeTestingServiceGrpc.getDoUpgradeTestsMethod = getDoUpgradeTestsMethod = 
              io.grpc.MethodDescriptor.<com.couchbase.grpc.protocol.UpgradeTestRequest, com.couchbase.grpc.protocol.UpgradeTestResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "protocol.UpgradeTestingService", "doUpgradeTests"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.couchbase.grpc.protocol.UpgradeTestRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.couchbase.grpc.protocol.UpgradeTestResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UpgradeTestingServiceMethodDescriptorSupplier("doUpgradeTests"))
                  .build();
          }
        }
     }
     return getDoUpgradeTestsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UpgradeTestingServiceStub newStub(io.grpc.Channel channel) {
    return new UpgradeTestingServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UpgradeTestingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UpgradeTestingServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UpgradeTestingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UpgradeTestingServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class UpgradeTestingServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void createConnection(com.couchbase.grpc.protocol.CreateConnectionRequest request,
        io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.CreateConnectionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateConnectionMethod(), responseObserver);
    }

    /**
     */
    public void doUpgradeTests(com.couchbase.grpc.protocol.UpgradeTestRequest request,
        io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.UpgradeTestResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDoUpgradeTestsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCreateConnectionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.couchbase.grpc.protocol.CreateConnectionRequest,
                com.couchbase.grpc.protocol.CreateConnectionResponse>(
                  this, METHODID_CREATE_CONNECTION)))
          .addMethod(
            getDoUpgradeTestsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.couchbase.grpc.protocol.UpgradeTestRequest,
                com.couchbase.grpc.protocol.UpgradeTestResponse>(
                  this, METHODID_DO_UPGRADE_TESTS)))
          .build();
    }
  }

  /**
   */
  public static final class UpgradeTestingServiceStub extends io.grpc.stub.AbstractStub<UpgradeTestingServiceStub> {
    private UpgradeTestingServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UpgradeTestingServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UpgradeTestingServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UpgradeTestingServiceStub(channel, callOptions);
    }

    /**
     */
    public void createConnection(com.couchbase.grpc.protocol.CreateConnectionRequest request,
        io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.CreateConnectionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateConnectionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void doUpgradeTests(com.couchbase.grpc.protocol.UpgradeTestRequest request,
        io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.UpgradeTestResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDoUpgradeTestsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class UpgradeTestingServiceBlockingStub extends io.grpc.stub.AbstractStub<UpgradeTestingServiceBlockingStub> {
    private UpgradeTestingServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UpgradeTestingServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UpgradeTestingServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UpgradeTestingServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.couchbase.grpc.protocol.CreateConnectionResponse createConnection(com.couchbase.grpc.protocol.CreateConnectionRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateConnectionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.couchbase.grpc.protocol.UpgradeTestResponse doUpgradeTests(com.couchbase.grpc.protocol.UpgradeTestRequest request) {
      return blockingUnaryCall(
          getChannel(), getDoUpgradeTestsMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class UpgradeTestingServiceFutureStub extends io.grpc.stub.AbstractStub<UpgradeTestingServiceFutureStub> {
    private UpgradeTestingServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UpgradeTestingServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UpgradeTestingServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UpgradeTestingServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.couchbase.grpc.protocol.CreateConnectionResponse> createConnection(
        com.couchbase.grpc.protocol.CreateConnectionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateConnectionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.couchbase.grpc.protocol.UpgradeTestResponse> doUpgradeTests(
        com.couchbase.grpc.protocol.UpgradeTestRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDoUpgradeTestsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_CONNECTION = 0;
  private static final int METHODID_DO_UPGRADE_TESTS = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UpgradeTestingServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(UpgradeTestingServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_CONNECTION:
          serviceImpl.createConnection((com.couchbase.grpc.protocol.CreateConnectionRequest) request,
              (io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.CreateConnectionResponse>) responseObserver);
          break;
        case METHODID_DO_UPGRADE_TESTS:
          serviceImpl.doUpgradeTests((com.couchbase.grpc.protocol.UpgradeTestRequest) request,
              (io.grpc.stub.StreamObserver<com.couchbase.grpc.protocol.UpgradeTestResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class UpgradeTestingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UpgradeTestingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.couchbase.grpc.protocol.Framework.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UpgradeTestingService");
    }
  }

  private static final class UpgradeTestingServiceFileDescriptorSupplier
      extends UpgradeTestingServiceBaseDescriptorSupplier {
    UpgradeTestingServiceFileDescriptorSupplier() {}
  }

  private static final class UpgradeTestingServiceMethodDescriptorSupplier
      extends UpgradeTestingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    UpgradeTestingServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UpgradeTestingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UpgradeTestingServiceFileDescriptorSupplier())
              .addMethod(getCreateConnectionMethod())
              .addMethod(getDoUpgradeTestsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
