
PROTO_SRCS = $(wildcard java/guicing/proto/*.proto)

all: proto

proto: $(PROTO_SRCS)
	protoc --java_out=genfiles $(PROTO_SRCS)
