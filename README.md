# kbgv

kbgv is a tool for reading from and writing to .bgv files. These files are used with various tools to render a graphical
visualization of a graph tree.

## Resources

The following resources were used to create this tool:

- Seafoam's [bgv.md](https://github.com/Shopify/seafoam/blob/master/docs/bgv.md) file, which gives a very helpful 
  high-level overview of the .bgv file structure.
- GraalVM's [GraphProtocol](https://github.com/oracle/graal/blob/master/compiler/src/org.graalvm.graphio/src/org/graalvm/graphio/GraphProtocol.java)
  class, which is the concrete implementation that bgv.md was derived from.