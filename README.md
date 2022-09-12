<div>
<h1 align="center">
    <br>
      <img align="left" src="https://github.com/Adrestus-net/Adrestus/blob/master/files/blocckhain.gif" alt="Snow" width="200"></td>
      <img src="./files/logo.jpg" alt="Forest"width="200"></td>
      <img align="right" width="200" src="https://github.com/Adrestus-net/Adrestus/blob/master/files/blocckhain.gif" style="box-sizing: border-box;"></td>
   <br>
</h1>
</div>
<div>
<h2 align="center">
  Java implementation of Adrestus Protocol
</h2>
</div>
<h4 align="center">Let’s Take Money Into The 21st Century with <a href="https://www.adrestus.net/">Adrestus</a>.</h4>

<div align="center">

<a href="">![build-status](https://ci.appveyor.com/api/projects/status/github/Adrestus-net/Adrestus?branch=master&svg=true)</a>
<a href="">[![Scc Count Badge](https://sloc.xyz/github/Adrestus-net/Adrestus/?category=lines)](https://github.com/Adrestus-net/Adrestus/)</a>
<a href="">[![codecov](https://codecov.io/github/Adrestus-net/Adrestus/branch/master/graph/badge.svg?token=1d910d2b-5749-4c90-bc7d-e8e4b3185606)](https://app.codecov.io/gh/Adrestus-net/Adrestus)</a>
<a href="">[![Lines-of-Code](https://hitsofcode.com/github/Adrestus-net/Adrestus)](https://github.com/Adrestus-net/Adrestus)</a>
<a href="">![GitHub Stats](https://komarev.com/ghpvc/?username=Adrestus-net)</a>
<a href="">[![GitHub stars](https://badgen.net/github/stars/Adrestus-net/Adrestus/)](https://github.com/Adrestus-net/Adrestus)</a>
<a href="">[![GitHub forks](https://badgen.net/github/forks/Adrestus-net/Adrestus/)](https://github.com/Adrestus-net/Adrestus)</a>
<a href="">[![GitHub contributors](https://badgen.net/github/contributors/Adrestus-net/Adrestus/)](https://github.com/Adrestus-net/Adrestus)</a>
<a href="">[![GitHub Issues](https://img.shields.io/github/issues/Adrestus-net/Adrestus.svg)](https://github.com/Adrestus-net/Adrestus/issues)</a>
<a href="">[![GitHub pull requests](https://img.shields.io/github/issues-pr/Adrestus-net/Adrestus.svg)](https://github.com/Adrestus-net/Adrestus/pulls)</a>
<a href="">[![GitHub commit](https://img.shields.io/github/last-commit/Adrestus-net/Adrestus.svg)]()</a>
<a href="">[![GitHub chat](https://img.shields.io/gitter/room/Adrestus-net/Adrestus.svg)](https://github.com/Adrestus-net/Adrestus/discussions/)</a>
<a href="">[![GitHub size](https://img.shields.io/github/directory-file-count/Adrestus-net/Adrestus.svg)]()</a>
<a href="">[![GitHub code-size](https://img.shields.io/github/languages/code-size/Adrestus-net/Adrestus.svg)]()</a>
<a href="">[![GitHub repo-size](https://img.shields.io/github/repo-size/Adrestus-net/Adrestus.svg)]()</a>
<a href="">[![License](https://img.shields.io/badge/license-Apache-green.svg)](https://github.com/Adrestus-net/Adrestus/blob/master/LICENSE)</a>
</div>

<div align="center">

<a  href="">![Github stats](https://github-readme-stats.vercel.app/api?username=PanagiotisDrakatos&theme=blue-green)</a>
<a  href="">![Github stats2](https://github-readme-streak-stats.herokuapp.com/?user=PanagiotisDrakatos&theme=blue-green)</a>
</div>

## Table of Contents

- [What’s Adrestus?](#What’s-Adrestus)
- [Building the Source Code](#Building-the-source)
    - [Install Java on Linux Platforms](#Install-Java)
    - [Running a full node](#Running-a-full-node)
    - [Hardware Requirements](#Hardware-Requirements)
    - [Quick Start Tool](#Quick-Start-Tool)
    - [Run inside Docker container](#Run-inside-Docker-container)
- [Package Organization](#Package-Organization)
- [Project Layout](#Project-Layout)
- [Progress](#Progress)
- [Community](#Community)
- [Contribution](#Contribution)
- [Resources](#Resources)
- [License](#License)

# What's Adrestus?

Adrestus is a project dedicated to building the infrastructure for a truly decentralized Internet.

* Adrestus Protocol, offers scalable, high-availability and high-throughput support that underlies all the decentralized
  applications in the Adrestus ecosystem.
* Adrestus enables large-scale development and engagement. With over 2000 transactions per second (TPS), high
  concurrency, low latency, and massive data transmission. It is ideal for building decentralized entertainment
  applications. Free features and incentive systems allow developers to create premium app experiences for users.

* Scalability, privacy, and interoperability issues have prevented widespread implementation of blockchain technology. A
  multi-tier blockchain network called Adrestus was created to handle these problems.

Our main premise is that numerous blockchains will be developed to address distinct business difficulties in distinct
industries. As a result, the Adrestus network is made to accommodate unique blockchain topologies and offer a trustless
method for interoperability between chains.

We go into greater information about our design and project roadmap in
the [Adrestus White Papers](https://www.sciencedirect.com/science/article/pii/S2096720922000343).

The primary (Java) kernel implementation and updates for the Adrestus Network are available in this repository.

# Building the source

Building java-Adrestus requires `git` and `Oracle JDK 1.11` to be installed, other JDK versions are not supported yet.
Make sure you operate on `Linux` , `MacOS`, `Windows` operating systems.

Clone the repo and switch to the `master` branch

  ```bash
  $ git clone https://github.com/Adrestus-net/Adrestus
  $ cd Adrestus
  $ git checkout -t origin/master
  ```

then run the following command to build java-Adrestus, the `adrestus-1.0-SNAPSHOT-jar-with-dependencies.jar` file can be
found in `java-Adrestus/build/libs/` after build successful.

```bash
$ .mvn clean build -x test
```

## Install Java on Linux Platforms

We highly recommend installing GraalVM instead of JDK for better performance

For existing Java applications, GraalVM can provide benefits by running them faster, providing extensibility via
scripting languages, or creating ahead-of-time compiled native images. Apply Graal, an advanced optimizing compiler,
that generates faster and leaner code requiring fewer compute resources Compile Java applications ahead-of-time to
native binaries that start up instantly and deliver peak performance with no warmup time

GraalVM can run in the context of OpenJDK to make Java applications run faster with a new just-in-time compilation
technology. GraalVM takes over the compilation of Java bytecode to machine code. In particular for other JVM-based
languages such as Scala, this configuration can achieve benefits, as for example experienced
by [Twitter running GraalVM in production](https://www.youtube.com/watch?v=pR5NDkIZBOA).

Follow these steps to install GraalVM Community Edition on the Linux operating system.

* Navigate to the GraalVM Releases repository on GitHub. Select Java 11 based based distribution for the Linux AMD64
  architecture, and download.

* Change the directory to the location where you want to install GraalVM, then move the .tar.gz archive to it.
* Unzip the archive:

  `tar -xzf graalvm-ce-java<version>-linux-amd64-<version>.tar.gz`
* There can be multiple JDKs installed on the machine. The next step is to configure the runtime environment:
    * Point the PATH environment variable to the GraalVM Enterprise bin directory:

      `export PATH=/path/to/<graalvm>/bin:$PATH`
    * Set the JAVA_HOME environment variable to resolve to the installation directory:

      `export JAVA_HOME=/path/to/<graalvm>`
* To check whether the installation was successful, run the `java -version` command.

Optionally, you can specify GraalVM as the default JRE or JDK installation in your Java IDE

## Running a full node

JVM tuning is a systematic and complex task but it is highly necessary in order to get greater performance from the
application. JVM tuning mostly entails improving garbage collection efficiency for applications running on virtual
machines (VMs) to have a higher throughput while consuming less memory and experiencing reduced latency. Less memory and
lower latency may not always equate to higher performance. It concerns making the best decision.

The three main ideas can make it simpler to apply garbage collection tweaking during the process in order to satisfy
desired application performance requirements.

* The goal of Minor GC is to reduce the frequency of Full GC for an application by collecting as many trash objects as
  it can each time.

* The GC memory maximization concept states that the garbage collection process is more effective and the application
  runs more smoothly when addressing throughput and latency issues. Throughput, latency, and memory use should only be
  tuned for two of the three performance attributes. This is known as the "two out of three" principle in general
  computation (GC).

* JVM tuning entails repeated iterations based on the outcomes of performance tests and ongoing configuration
  optimizations. Each of the preceding processes may go through several rounds before each target system measure is
  satisfied. It may be necessary to adjust the previous parameters several times in order to meet a particular metric,
  in which case all of the preceding procedures must be tried once more.

Thus we highly recommend to to run a full node with the following JVM parameters in mind.

```bash
   $ nohup java -Xms9G -Xmx9G -XX:ReservedCodeCacheSize=256m \
                -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m \
                -XX:MaxDirectMemorySize=1G -XX:+PrintGCDetails \
                -XX:+PrintGCDateStamps  -Xloggc:gc.log \
                -XX:+UseConcMarkSweepGC -XX:NewRatio=2 \
                -XX:+CMSScavengeBeforeRemark -XX:+ParallelRefProcEnabled \
                -XX:+HeapDumpOnOutOfMemoryError \
                -XX:+UseCMSInitiatingOccupancyOnly  -XX:CMSInitiatingOccupancyFraction=70 \
                -jar FullNode.jar >> start.log 2>&1 &
   ```

## Hardware Requirements

Minimum:

* CPU with 8 cores
* 16GB RAM
* 1TB free storage space to sync the Mainnet

Recommended:

* CPU with 16+ cores(32+ cores for a super representative)
* 32GB+ RAM(64GB+ for a super representative)
* High Performance SSD with at least 1.5TB free space
* 100+ MB/s download Internet service

The minimum hardware requirements are `CPU with 8 cores`,`16GB RAM` and `1TB free storage space` to sync the Mainnet,
the recommended hardware requirements:

* CPU with 16+ cores(32+ cores for a super representative)
* 32+ GB RAM(64+ GB for a super representative)
* High Performance SSD with at least 1.5TB free space
* 100+ MB/s download Internet service

## Quick Start Tool

An easier way to build and run java-Adrestus is to use `start.sh`, `start.sh` is a quick start script written in shell
language, you can use it to build and run java-Adrestus quickly and easily.

Here are some common use cases of the scripting tool

* Use `start.sh` to start a full node with the downloaded `FullNode.jar`
* Use `start.sh` to download the latest `FullNode.jar` and start a full node.
* Use `start.sh` to download the latest source code and compile a `FullNode.jar` and then start a full node.

## Run inside Docker container

One of the quickest ways to get `java-Adrestus` up and running on your machine is by using Docker:

```shell
$ docker run -d --name="java-Adrestus" \
             -v /your_path/output-directory:/java-Adrestus/output-directory \
             -v /your_path/logs:/java-Adrestus/logs \
             -p 8090:8090 -p 18888:18888 -p 50051:50051 \
             Adrestusprotocol/java-Adrestus \
             -c /java-Adrestus/config/main_net_config.conf
```

This will mount the `output-directory` and `logs` directories on the host, the docker.sh tool can also be used to
simplify the use of docker.

# Package Organization

The main folders are:

| Folder   | Content                          |
|----------|----------------------------------|
| `config`   | The [config](config) dependency.     |
| `consensus` | The [consensus](consensus) dependency. |
| `core`   | The [core](core) dependency.     |
| `crypto`    | The [crypto](crypto) dependency.       |
| `network`    | The [network](network) dependency.       |
| `protocol`    | The [protocol](protocol) dependency.       |
| `util`    | The [util](util) dependency.       |

# Project Layout

`Adrestus`  is split into various maven subpackages. The following packages provide core functionality to the Adrestus
ecosystem, as well as other tools and commands:

* `config` Holds configuration parameters. These include parameters used locally by the node as well as parameters that
  must be agreed upon by the protocol.
* `consensus` Contains the Adrestus Byzantine Fault tolerant Agreement protocol's agreement service. Under the condition
  that sufficient account stake is correctly executing the protocol, this protocol enables participating accounts to
  swiftly confirm blocks in a fork-safe way.
* `core` Different kinds used across the codebase are defined by data. basic kinds include things like addresses,
  account information, and macro algorithms. Accounts are classified as "root" accounts (which have financial authority)
  and "participation" accounts (which can participate in the agreement protocol). Accounts' ability to make transactions
  against the Adrestus state is defined by transactions. These include transactions for participation keys and normal
  payments. Blocks are groups of transactions that are atomically committed to Adrestus pools, which carry out the
  transaction pool and are defined by accounting. Before they are offered in a block, the transaction pool stores
  transactions that a node has viewed in memory. The agreement protocol's committee implements the credentials used to
  verify a participating account's membership. The Adrestus state machine, which maintains the block order, is also
  included. The state transitions that follow the application of these blocks are carried out by the core package.
* `ledjer`  It responds to questions about accounts and blocks, such as "What transactions were in the most recent
  committed block?" and "What is my balance?"
* `crypto` Includes the cryptographic building blocks used for hashing, signatures, VDFs, and VRFs. Adrestus-specific
  information regarding spending keys, protocols keys, one-time signing keys, and how they relate to one another may
  also be found here.
* `network` Consists of the code needed to join a Kademlia network using netty Sockets. Maintains connections with a set
  of peers, (optionally) accepts connections from peers, sends and receives point-to-point and broadcast messages, and
  routes messages received to different handler programs (such as network, agreement, or gossip).
* `protocol` Implements the Adrestus logic, start receiving transactions from sockets initiate the consensus timer, and
  every 2 sockets the organizer of each zone packet of blocks with a bunch of transactions and send them for
  verification across the validators
* `util` It includes helper functions or static methods that the project needs to build the core infrastructure

# Progress

### Done

- ✅ Cryptography
    - ✅ Mnemonic codes
    - ✅ Address Generation
    - ✅ Elliptic curves secp256k1
    - ✅ Elliptic curves Signature Verification (V,R,S)
    - ✅ Belare-Neven Signature
    - ✅ BLS Signature
    - ✅ Aggregated BLS Multi-signature
    - ✅ VRF
    - ✅ VDF
- ✅ Core
    - ✅ Transaction
    - ✅ Block
    - ✅ Account
    - ✅ Merklee Trie with proofs
    - ✅ Optimized Merklee Patricia Trie
    - ✅ Ring buffer implementation for parallel execution
- ✅ Execution
    - ✅ Transaction
    - ✅ Block
    - ✅ State update
    - ✅ Synchronization
    - ✅ Read/Writer Locks
    - ✅ Zone Fork choice
- ✅ Message Handler
    - ✅ Transaction dispatcher
    - ✅ Transaction
    - ✅ State
    - ✅ Network - Message dispatching
- ✅ Network
    - ✅ Optimized wiring protocol
    - ✅ publish/subscribe
- ✅ Optimizations
    - ✅ Randomness
    - ✅ Consensus
- ✅ Testing
    - ✅ Unit tests
    - ✅ Integration tests
    - ✅ TeamCity continuous integration
    - ✅ Manual testing

### In progress

- [ ] Staking/Delegation
- [ ] Wallet Integration
- [ ] Network
    - [ ] kademlia network discovery
- [ ] Light Nodes Syncing
- [ ] Consensus
    - [ ] change-view-protocol
- [ ] Zones
    - [ ] Nodes dispatcher (shuffling)
- [ ] VM
    - [ ] EVM Core
- [ ] Testing
    - [ ] Automate tests with AWS
    - [ ] Nodes Monitoring
- [ ] Smart Contracts
- [ ] Privacy
    - [ ] Bloom Filters integration
- [ ] Governance
    - [ ] Concept reviewed
- [ ] Economics
    - [ ] Concept reviewed
- [ ] Interoperability
- [ ] Optimizations
- [ ] Bugfixing

# Community

We plan in the near future to add a telegram and discord channel for the community members

# Contribution

Thank you for considering to help out with the source code! We welcome contributions from anyone on the internet, and
are grateful for even the smallest of fixes to Adrestus!
If you'd like to contribute to java-Adrestus, please see the [Contribution Guide](./CONTRIBUTING.md) for more details.

Please fork, fix, commit and send a pull request for the maintainers to review and merge into the main code base. If you
wish to submit more complex changes though, please check up with the core developers first here on github, to ensure
those changes are in line with the general philosophy of the project and/or get some early feedback which can make both
your efforts much lighter as well as our review and merge procedures quick and simple.

# Resources

* [Website](https://www.adrestus.net/) java-Asrestus official website.
* [Documentation](https://www.sciencedirect.com/science/article/pii/S2096720922000343) Adrestus official technical
  documentation website.

# License

java-Adrestus is released under the [Apache License 2.0](https://github.com/Adrestus-net/Adrestus/blob/master/LICENSE).
