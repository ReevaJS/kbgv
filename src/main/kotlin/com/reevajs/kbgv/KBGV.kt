package com.reevajs.kbgv

import com.reevajs.kbgv.objects.*

@Suppress("unused", "FunctionName", "SpellCheckingInspection")
class KBGV {
    private var nodeId: Int = 0
    private var poolId: UShort = 0U
    private var graphId: Int = 0
    private var blockId: Int = 0

    fun assemble(components: List<IBGVGroupDocumentGraph>) = BGVObject(
        BGVToken.MAJOR_VERSION,
        BGVToken.MINOR_VERSION,
        components,
    )

    fun Group(
        name: BGVStringPool,
        shortName: BGVStringPool,
        method: BGVMethodPool?,
        bci: Int,
        props: BGVProps,
        children: List<IBGVGroupDocumentGraph>,
    ) = BGVGroup(name, shortName, method, bci, props, children)

    fun Document(props: BGVProps) = BGVDocument(props)

    fun Graph(
        name: String,
        args: List<IBGVPropObject>,
        props: BGVProps,
        nodes: List<BGVNode>,
        blocks: List<BGVBlocks>,
    ) = BGVGraph(graphId++, name, args, BGVGraphBody(props, nodes, blocks))

    fun Node(
        nodeClass: BGVNodeClassPool,
        hasPredecessor: Boolean,
        props: BGVProps,
        edgesIn: List<IBGVEdge>,
        edgesOut: List<IBGVEdge>,
    ) = BGVNode(nodeId++, nodeClass, hasPredecessor, props, edgesIn, edgesOut)

    fun Blocks(nodes: List<Int>, successors: List<Int>) = BGVBlocks(blockId++, nodes, successors)

    fun DirectEdge(node: Int) = BGVDirectEdge(node)

    fun IndirectEdge(nodes: List<Int>) = BGVIndirectEdge(nodes)

    fun InputEdgeInfo(
        indirect: Boolean,
        name: BGVStringPool,
        type: IBGVPoolObject,
    ) = BGVInputEdgeInfo(indirect, name, type)

    fun OutputEdgeInfo(indirect: Boolean, name: BGVStringPool) = BGVOutputEdgeInfo(indirect, name)

    fun Props(props: List<BGVProp>) = BGVProps(props)

    fun Props(props: Map<BGVStringPool, IBGVPropObject>) = BGVProps(props.entries.map { Prop(it.key, it.value) })

    fun Prop(key: BGVStringPool, value: IBGVPropObject) = BGVProp(key, value)

    fun Int(value: Int) = BGVIntProperty(value)

    fun Long(value: Long) = BGVLongProperty(value)

    fun Double(value: Double) = BGVDoubleProperty(value)

    fun Float(value: Float) = BGVFloatProperty(value)

    fun True() = BGVTrueProperty

    fun False() = BGVFalseProperty

    fun Array(ints: IntArray) = BGVArrayProperty(ints.toList())

    fun Array(doubles: DoubleArray) = BGVArrayProperty(doubles.toList())

    fun Array(arr: List<Any>) = BGVArrayProperty(arr)

    fun GraphBody(graph: BGVGraphBody) = BGVSubgraphProperty(graph)

    fun NullPool() = BGVNullPool

    fun StringPool(value: String) = BGVStringPool(poolId++, value)

    fun EnumPool(enumClass: BGVClassPool, ordinal: Int) = BGVEnumPool(poolId++, enumClass, ordinal)

    fun ClassPool(typeName: String) = BGVClassPool(poolId++, typeName, BGVClassPoolKlassType)

    fun ClassPool(typeName: String, values: List<BGVStringPool>) =
        BGVClassPool(poolId++, typeName, BGVClassPoolEnumType(values))

    fun MethodPool(
        declaringClass: BGVClassPool,
        methodName: BGVStringPool,
        signature: BGVNodeSignaturePool,
        modifiers: Int,
        bytes: ByteArray,
    ) = BGVMethodPool(poolId++, declaringClass, methodName, signature, modifiers, bytes)

    fun NodeClassPool(
        nodeClass: BGVClassPool,
        nameTemplate: String,
        inputs: List<BGVInputEdgeInfo>,
        outputs: List<BGVOutputEdgeInfo>,
    ) = BGVNodeClassPool(poolId++, nodeClass, nameTemplate, inputs, outputs)

    fun FieldPool(
        fieldClass: BGVClassPool,
        name: BGVStringPool,
        typeName: BGVStringPool,
        modifiers: Int,
    ) = BGVFieldPool(poolId++, fieldClass, name, typeName, modifiers)

    fun NodeSignaturePool(
        method: BGVMethodPool,
        bci: Int,
        sourcePositions: List<SourcePosition>,
        caller: BGVNodeSourcePositionPool?,
    ) = BGVNodeSourcePositionPool(poolId++, method, bci, sourcePositions, caller)

    fun NodePool(
        nodeId: Int,
        nodeClass: BGVNodeClassPool,
    ) = BGVNodePool(poolId++, nodeId, nodeClass)
}
