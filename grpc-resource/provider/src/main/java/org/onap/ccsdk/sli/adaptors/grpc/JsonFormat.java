// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package org.onap.ccsdk.sli.adaptors.grpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//Fork from :
//
//<dependency>
//<groupId>com.google.protobuf</groupId>
//<artifactId>protobuf-java-util</artifactId>
//<version>3.6.1</version>
//</dependency>

public class JsonFormat {

    private JsonFormat() {
    }

    public static JsonFormat.Printer printer() {
        return new JsonFormat.Printer(JsonFormat.TypeRegistry.getEmptyTypeRegistry(), false, Collections.emptySet(),
            false, false, false);
    }

    public static JsonFormat.Parser parser() {
        return new JsonFormat.Parser(JsonFormat.TypeRegistry.getEmptyTypeRegistry(), false, 100);
    }

    private static class ParserImpl {

        private final JsonFormat.TypeRegistry registry;
        private final JsonParser jsonParser;
        private final boolean ignoringUnknownFields;
        private final int recursionLimit;
        private int currentDepth;
        private static final Map<String, JsonFormat.ParserImpl.WellKnownTypeParser> wellKnownTypeParsers = buildWellKnownTypeParsers();
        private final Map<Descriptor, Map<String, FieldDescriptor>> fieldNameMaps = new HashMap();
        private static final BigInteger MAX_UINT64 = new BigInteger("FFFFFFFFFFFFFFFF", 16);
        private static final double EPSILON = 1.0E-6D;
        private static final BigDecimal MORE_THAN_ONE = new BigDecimal(String.valueOf(1.000001D));
        private static final BigDecimal MAX_DOUBLE;
        private static final BigDecimal MIN_DOUBLE;

        ParserImpl(JsonFormat.TypeRegistry registry, boolean ignoreUnknownFields, int recursionLimit) {
            this.registry = registry;
            this.ignoringUnknownFields = ignoreUnknownFields;
            this.jsonParser = new JsonParser();
            this.recursionLimit = recursionLimit;
            this.currentDepth = 0;
        }

        void merge(String json, com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
            try {
                JsonReader reader = new JsonReader(new StringReader(json));
                reader.setLenient(false);
                this.merge(this.jsonParser.parse(reader), builder);
            } catch (InvalidProtocolBufferException var4) {
                throw var4;
            } catch (Exception var5) {
                throw new InvalidProtocolBufferException(var5.getMessage());
            }
        }

        private static Map<String, JsonFormat.ParserImpl.WellKnownTypeParser> buildWellKnownTypeParsers() {
            Map<String, JsonFormat.ParserImpl.WellKnownTypeParser> parsers = new HashMap();
            parsers.put(Any.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                }
            });
            JsonFormat.ParserImpl.WellKnownTypeParser wrappersPrinter = new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                }
            };
            parsers.put(BoolValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Int32Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(UInt32Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Int64Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(UInt64Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(StringValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(BytesValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(FloatValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(DoubleValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Struct.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeStruct(json, builder);
                }
            });
            parsers.put(ListValue.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                }
            });
            parsers.put(Value.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeValue(json, builder);
                }
            });
            return parsers;
        }

        private void merge(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            JsonFormat.ParserImpl.WellKnownTypeParser specialParser = (JsonFormat.ParserImpl.WellKnownTypeParser) wellKnownTypeParsers
                .get(builder.getDescriptorForType().getFullName());
            if (specialParser != null) {
                specialParser.merge(this, json, builder);
            }
        }


        private void mergeStruct(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor descriptor = builder.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("fields");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid Struct type.");
            } else {
                this.mergeMapField(field, json, builder);
            }
        }

        private void mergeValue(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor type = builder.getDescriptorForType();
            if (json instanceof JsonPrimitive) {
                JsonPrimitive primitive = (JsonPrimitive) json;
                if (primitive.isBoolean()) {
                    builder.setField(type.findFieldByName("bool_value"), primitive.getAsBoolean());
                } else if (primitive.isNumber()) {
                    builder.setField(type.findFieldByName("number_value"), primitive.getAsDouble());
                } else {
                    builder.setField(type.findFieldByName("string_value"), primitive.getAsString());
                }
            } else {
                com.google.protobuf.Message.Builder listBuilder;
                FieldDescriptor field;
                if (json instanceof JsonObject) {
                    field = type.findFieldByName("struct_value");
                    listBuilder = builder.newBuilderForField(field);
                    this.merge(json, listBuilder);
                    builder.setField(field, listBuilder.build());
                } else if (json instanceof JsonArray) {
                    field = type.findFieldByName("list_value");
                    listBuilder = builder.newBuilderForField(field);
                    this.merge(json, listBuilder);
                    builder.setField(field, listBuilder.build());
                } else {
                    if (!(json instanceof JsonNull)) {
                        throw new IllegalStateException("Unexpected json data: " + json);
                    }

                    builder.setField(type.findFieldByName("null_value"), NullValue.NULL_VALUE.getValueDescriptor());
                }
            }

        }

        private void mergeMapField(FieldDescriptor field, JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            if (!(json instanceof JsonObject)) {
                throw new InvalidProtocolBufferException("Expect a map object but found: " + json);
            } else {
                Descriptor type = field.getMessageType();
                FieldDescriptor keyField = type.findFieldByName("key");
                FieldDescriptor valueField = type.findFieldByName("value");
                if (keyField != null && valueField != null) {
                    JsonObject object = (JsonObject) json;
                    Iterator var8 = object.entrySet().iterator();

                    while (var8.hasNext()) {
                        Entry<String, JsonElement> entry = (Entry) var8.next();
                        com.google.protobuf.Message.Builder entryBuilder = builder.newBuilderForField(field);
                        Object key = this
                            .parseFieldValue(keyField, new JsonPrimitive((String) entry.getKey()), entryBuilder);
                        Object value = this.parseFieldValue(valueField, (JsonElement) entry.getValue(), entryBuilder);
                        if (value == null) {
                            throw new InvalidProtocolBufferException("Map value cannot be null.");
                        }

                        entryBuilder.setField(keyField, key);
                        entryBuilder.setField(valueField, value);
                        builder.addRepeatedField(field, entryBuilder.build());
                    }

                } else {
                    throw new InvalidProtocolBufferException("Invalid map field: " + field.getFullName());
                }
            }
        }

        private String parseString(JsonElement json) {
            return json.getAsString();
        }

        private Object parseFieldValue(FieldDescriptor field, JsonElement json,
            com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
            if (json instanceof JsonNull) {
                if (field.getJavaType() == JavaType.MESSAGE && field.getMessageType().getFullName()
                    .equals(Value.getDescriptor().getFullName())) {
                    Value value = Value.newBuilder().setNullValueValue(0).build();
                    return builder.newBuilderForField(field).mergeFrom(value.toByteString()).build();
                } else {
                    return field.getJavaType() == JavaType.ENUM && field.getEnumType().getFullName()
                        .equals(NullValue.getDescriptor().getFullName()) ? field.getEnumType().findValueByNumber(0)
                        : null;
                }
            } else {
                switch (field.getType()) {
                    case STRING:
                        return this.parseString(json);
                    case MESSAGE:
                    case GROUP:
                        if (this.currentDepth >= this.recursionLimit) {
                            throw new InvalidProtocolBufferException("Hit recursion limit.");
                        }

                        ++this.currentDepth;
                        com.google.protobuf.Message.Builder subBuilder = builder.newBuilderForField(field);
                        this.merge(json, subBuilder);
                        --this.currentDepth;
                        return subBuilder.build();
                    default:
                        throw new InvalidProtocolBufferException("Invalid field type: " + field.getType());
                }
            }
        }

        static {
            MAX_DOUBLE = (new BigDecimal(String.valueOf(1.7976931348623157E308D))).multiply(MORE_THAN_ONE);
            MIN_DOUBLE = (new BigDecimal(String.valueOf(-1.7976931348623157E308D))).multiply(MORE_THAN_ONE);
        }

        private interface WellKnownTypeParser {

            void merge(JsonFormat.ParserImpl var1, JsonElement var2, com.google.protobuf.Message.Builder var3)
                throws InvalidProtocolBufferException;
        }
    }

    private static final class PrinterImpl {

        private final JsonFormat.TypeRegistry registry;
        private final boolean alwaysOutputDefaultValueFields;
        private final Set<FieldDescriptor> includingDefaultValueFields;
        private final boolean preservingProtoFieldNames;
        private final boolean printingEnumsAsInts;
        private final JsonFormat.TextGenerator generator;
        private final Gson gson;
        private final CharSequence blankOrSpace;
        private final CharSequence blankOrNewLine;
        private static final Map<String, JsonFormat.PrinterImpl.WellKnownTypePrinter> wellKnownTypePrinters = buildWellKnownTypePrinters();

        PrinterImpl(JsonFormat.TypeRegistry registry, boolean alwaysOutputDefaultValueFields,
            Set<FieldDescriptor> includingDefaultValueFields, boolean preservingProtoFieldNames, Appendable jsonOutput,
            boolean omittingInsignificantWhitespace, boolean printingEnumsAsInts) {
            this.registry = registry;
            this.alwaysOutputDefaultValueFields = alwaysOutputDefaultValueFields;
            this.includingDefaultValueFields = includingDefaultValueFields;
            this.preservingProtoFieldNames = preservingProtoFieldNames;
            this.printingEnumsAsInts = printingEnumsAsInts;
            this.gson = JsonFormat.PrinterImpl.GsonHolder.DEFAULT_GSON;
            if (omittingInsignificantWhitespace) {
                this.generator = new JsonFormat.CompactTextGenerator(jsonOutput);
                this.blankOrSpace = "";
                this.blankOrNewLine = "";
            } else {
                this.generator = new JsonFormat.PrettyTextGenerator(jsonOutput);
                this.blankOrSpace = " ";
                this.blankOrNewLine = "\n";
            }

        }

        void print(MessageOrBuilder message) throws IOException {
            JsonFormat.PrinterImpl.WellKnownTypePrinter specialPrinter = (JsonFormat.PrinterImpl.WellKnownTypePrinter) wellKnownTypePrinters
                .get(message.getDescriptorForType().getFullName());
            if (specialPrinter != null) {
                specialPrinter.print(this, message);
            }
        }

        private static Map<String, JsonFormat.PrinterImpl.WellKnownTypePrinter> buildWellKnownTypePrinters() {
            Map<String, JsonFormat.PrinterImpl.WellKnownTypePrinter> printers = new HashMap();
            printers.put(Any.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                }
            });
            JsonFormat.PrinterImpl.WellKnownTypePrinter wrappersPrinter = new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                }
            };
            printers.put(BoolValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Int32Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(UInt32Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Int64Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(UInt64Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(StringValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(BytesValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(FloatValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(DoubleValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Struct.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printStruct(message);
                }
            });
            printers.put(Value.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printValue(message);
                }
            });
            printers.put(ListValue.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                }
            });
            return printers;
        }

        private void printStruct(MessageOrBuilder message) throws IOException {
            Descriptor descriptor = message.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("fields");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid Struct type.");
            } else {
                this.printMapFieldValue(field, message.getField(field));
            }
        }

        private void printValue(MessageOrBuilder message) throws IOException {
            Map<FieldDescriptor, Object> fields = message.getAllFields();
            if (fields.isEmpty()) {
                this.generator.print("null");
            } else if (fields.size() != 1) {
                throw new InvalidProtocolBufferException("Invalid Value type.");
            } else {
                Iterator var3 = fields.entrySet().iterator();

                while (var3.hasNext()) {
                    Entry<FieldDescriptor, Object> entry = (Entry) var3.next();
                    this.printSingleFieldValue((FieldDescriptor) entry.getKey(), entry.getValue());
                }

            }
        }

        private void printMapFieldValue(FieldDescriptor field, Object value) throws IOException {
            Descriptor type = field.getMessageType();
            FieldDescriptor keyField = type.findFieldByName("key");
            FieldDescriptor valueField = type.findFieldByName("value");
            if (keyField != null && valueField != null) {
                this.generator.print("{" + this.blankOrNewLine);
                this.generator.indent();
                boolean printedElement = false;
                Iterator var7 = ((List) value).iterator();

                while (var7.hasNext()) {
                    Object element = var7.next();
                    Message entry = (Message) element;
                    Object entryKey = entry.getField(keyField);
                    Object entryValue = entry.getField(valueField);
                    if (printedElement) {
                        this.generator.print("," + this.blankOrNewLine);
                    } else {
                        printedElement = true;
                    }

                    this.printSingleFieldValue(keyField, entryKey, true);
                    this.generator.print(":" + this.blankOrSpace);
                    this.printSingleFieldValue(valueField, entryValue);
                }

                if (printedElement) {
                    this.generator.print(this.blankOrNewLine);
                }

                this.generator.outdent();
                this.generator.print("}");
            } else {
                throw new InvalidProtocolBufferException("Invalid map field.");
            }
        }

        private void printSingleFieldValue(FieldDescriptor field, Object value) throws IOException {
            this.printSingleFieldValue(field, value, false);
        }

        private void printSingleFieldValue(FieldDescriptor field, Object value, boolean alwaysWithQuotes)
            throws IOException {
            switch (field.getType()) {
                case DOUBLE:
                    Double doubleValue = (Double) value;
                    if (doubleValue.isNaN()) {
                        this.generator.print("\"NaN\"");
                    } else if (doubleValue.isInfinite()) {
                        if (doubleValue < 0.0D) {
                            this.generator.print("\"-Infinity\"");
                        } else {
                            this.generator.print("\"Infinity\"");
                        }
                    } else {
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }

                        this.generator.print(doubleValue.toString());
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }
                    }
                    break;
                case STRING:
                    this.generator.print(this.gson.toJson(value));
                    break;
                case MESSAGE:
                case GROUP:
                    this.print((Message) value);
            }

        }

        private interface WellKnownTypePrinter {

            void print(JsonFormat.PrinterImpl var1, MessageOrBuilder var2) throws IOException;
        }

        private static class GsonHolder {

            private static final Gson DEFAULT_GSON = (new GsonBuilder()).disableHtmlEscaping().create();

            private GsonHolder() {
            }
        }
    }

    private static final class PrettyTextGenerator implements JsonFormat.TextGenerator {

        private final Appendable output;
        private final StringBuilder indent;
        private boolean atStartOfLine;

        private PrettyTextGenerator(Appendable output) {
            this.indent = new StringBuilder();
            this.atStartOfLine = true;
            this.output = output;
        }

        public void indent() {
            this.indent.append("  ");
        }

        public void outdent() {
            int length = this.indent.length();
            if (length < 2) {
                throw new IllegalArgumentException(" Outdent() without matching Indent().");
            } else {
                this.indent.delete(length - 2, length);
            }
        }

        public void print(CharSequence text) throws IOException {
            int size = text.length();
            int pos = 0;

            for (int i = 0; i < size; ++i) {
                if (text.charAt(i) == '\n') {
                    this.write(text.subSequence(pos, i + 1));
                    pos = i + 1;
                    this.atStartOfLine = true;
                }
            }

            this.write(text.subSequence(pos, size));
        }

        private void write(CharSequence data) throws IOException {
            if (data.length() != 0) {
                if (this.atStartOfLine) {
                    this.atStartOfLine = false;
                    this.output.append(this.indent);
                }

                this.output.append(data);
            }
        }
    }

    private static final class CompactTextGenerator implements JsonFormat.TextGenerator {

        private final Appendable output;

        private CompactTextGenerator(Appendable output) {
            this.output = output;
        }

        public void indent() {
        }

        public void outdent() {
        }

        public void print(CharSequence text) throws IOException {
            this.output.append(text);
        }
    }

    interface TextGenerator {

        void indent();

        void outdent();

        void print(CharSequence var1) throws IOException;
    }

    public static class TypeRegistry {

        private final Map<String, Descriptor> types;

        public static JsonFormat.TypeRegistry getEmptyTypeRegistry() {
            return JsonFormat.TypeRegistry.EmptyTypeRegistryHolder.EMPTY;
        }

        public Descriptor find(String name) {
            return (Descriptor) this.types.get(name);
        }

        private TypeRegistry(Map<String, Descriptor> types) {
            this.types = types;
        }

        private static class EmptyTypeRegistryHolder {

            private static final JsonFormat.TypeRegistry EMPTY = new JsonFormat.TypeRegistry(Collections.emptyMap());

            private EmptyTypeRegistryHolder() {
            }
        }
    }

    public static class Parser {

        private final JsonFormat.TypeRegistry registry;
        private final boolean ignoringUnknownFields;
        private final int recursionLimit;
        private static final int DEFAULT_RECURSION_LIMIT = 100;

        private Parser(JsonFormat.TypeRegistry registry, boolean ignoreUnknownFields, int recursionLimit) {
            this.registry = registry;
            this.ignoringUnknownFields = ignoreUnknownFields;
            this.recursionLimit = recursionLimit;
        }

        public void merge(String json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            (new JsonFormat.ParserImpl(this.registry, this.ignoringUnknownFields, this.recursionLimit))
                .merge(json, builder);
        }
    }

    public static class Printer {

        private final JsonFormat.TypeRegistry registry;
        private boolean alwaysOutputDefaultValueFields;
        private Set<FieldDescriptor> includingDefaultValueFields;
        private final boolean preservingProtoFieldNames;
        private final boolean omittingInsignificantWhitespace;
        private final boolean printingEnumsAsInts;

        private Printer(JsonFormat.TypeRegistry registry, boolean alwaysOutputDefaultValueFields,
            Set<FieldDescriptor> includingDefaultValueFields, boolean preservingProtoFieldNames,
            boolean omittingInsignificantWhitespace, boolean printingEnumsAsInts) {
            this.registry = registry;
            this.alwaysOutputDefaultValueFields = alwaysOutputDefaultValueFields;
            this.includingDefaultValueFields = includingDefaultValueFields;
            this.preservingProtoFieldNames = preservingProtoFieldNames;
            this.omittingInsignificantWhitespace = omittingInsignificantWhitespace;
            this.printingEnumsAsInts = printingEnumsAsInts;
        }

        public void appendTo(MessageOrBuilder message, Appendable output) throws IOException {
            (new JsonFormat.PrinterImpl(this.registry, this.alwaysOutputDefaultValueFields,
                this.includingDefaultValueFields, this.preservingProtoFieldNames, output,
                this.omittingInsignificantWhitespace, this.printingEnumsAsInts)).print(message);
        }

        public String print(MessageOrBuilder message) throws InvalidProtocolBufferException {
            try {
                StringBuilder builder = new StringBuilder();
                this.appendTo(message, builder);
                return builder.toString();
            } catch (InvalidProtocolBufferException var3) {
                throw var3;
            } catch (IOException var4) {
                throw new IllegalStateException(var4);
            }
        }
    }
}
