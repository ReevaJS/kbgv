package com.reevajs.kbgv

import com.reevajs.kbgv.objects.*

@Suppress("unused", "FunctionName", "SpellCheckingInspection")
class KBGV {
    private var nodeId: Int = 0
    private var poolId: UShort = 0U

    fun assemble(components: Collection<IBGVGroupDocumentGraph>) = BGVObject(
        BGVToken.MAJOR_VERSION,
        BGVToken.MINOR_VERSION,
        components,
    )

    fun Document(props: BGVProps) = BGVDocument(props)

    fun Node(
        nodeClass: BGVNodeClassPool,
        hasPredecessor: Boolean,
        props: BGVProps,
        edgesIn: Collection<BGVEdge>,
        edgesOut: Collection<BGVEdge>,
    ) = BGVNode(nodeId++, nodeClass, hasPredecessor, props, edgesIn, edgesOut)

    fun Int(value: Int) = BGVIntProperty(value)

    fun Long(value: Long) = BGVLongProperty(value)

    fun Double(value: Double) = BGVDoubleProperty(value)

    fun Float(value: Float) = BGVFloatProperty(value)

    fun True() = BGVTrueProperty

    fun False() = BGVFalseProperty

    fun Array(ints: IntArray) = BGVArrayProperty(ints.toList())

    fun Array(doubles: DoubleArray) = BGVArrayProperty(doubles.toList())

    fun Array(arr: Collection<Any>) = BGVArrayProperty(arr)

    fun Subgraph(graph: BGVGraphBody) = BGVSubgraphProperty(graph)

    fun NullPool() = BGVNullPool

    fun StringPool(value: String) = BGVStringPool(poolId++, value)

    fun EnumPool(enumClass: IBGVPoolObject, ordinal: Int) = BGVEnumPool(poolId++, enumClass, ordinal)

    fun ClassPool(typeName: String) = BGVClassPool(poolId++, typeName, BGVClassPoolKlassType)

    fun ClassPool(typeName: String, values: Collection<IBGVPoolObject>) =
        BGVClassPool(poolId++, typeName, BGVClassPoolEnumType(values))

    fun MethodPool(
        declaringClass: IBGVPoolObject,
        methodName: IBGVPoolObject,
        signature: IBGVPoolObject,
        modifiers: Int,
        bytes: ByteArray,
    ) = BGVMethodPool(poolId++, declaringClass, methodName, signature, modifiers, bytes)

    fun NodeClassPool(
        nodeClass: IBGVPoolObject,
        nameTemplate: String,
        inputs: Collection<BGVInputEdgeInfo>,
        outputs: Collection<BGVOutputEdgeInfo>,
    ) = BGVNodeClassPool(poolId++, nodeClass, nameTemplate, inputs, outputs)

    fun FieldPool(
        fieldClass: IBGVPoolObject,
        name: IBGVPoolObject,
        typeName: IBGVPoolObject,
        modifiers: Int,
    ) = BGVFieldPool(poolId++, fieldClass, name, typeName, modifiers)

    fun NodeSignaturePool(
        method: IBGVPoolObject,
        bci: Int,
        sourcePositions: Collection<BGVSourcePosition>,
        caller: IBGVPoolObject,
    ) = BGVNodeSourcePositionPool(poolId++, method, bci, sourcePositions, caller)

    fun NodePool(
        nodeId: Int,
        nodeClass: IBGVPoolObject,
    ) = BGVNodePool(poolId++, nodeId, nodeClass)

    fun PoolRef(id: Int, type: Byte) = BGVPoolReference(type, id.toUShort())
}
