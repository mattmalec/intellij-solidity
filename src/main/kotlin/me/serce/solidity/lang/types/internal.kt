package me.serce.solidity.lang.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import me.serce.solidity.lang.psi.SolPsiFactory
import me.serce.solidity.lang.types.SolInteger.Companion.UINT_256

class SolInternalTypeFactory(project: Project) {
  private val psiFactory: SolPsiFactory = SolPsiFactory(project)

  companion object {
    fun of(project: Project): SolInternalTypeFactory {
      return ServiceManager.getService(project, SolInternalTypeFactory::class.java)
    }
  }

  private val registry: Map<String, SolType> by lazy {
    listOf(
      msgType,
      txType,
      blockType
    ).associateBy { it.toString() }
  }

  fun byName(name: String): SolType? = registry[name]

  val msgType: SolType by lazy {
    SolStruct(psiFactory.createStruct("""
      struct ${internalise("Msg")} {
          bytes data;
          uint gas;
          address sender;
          uint value;
          uint tokenvalue;
          uint tokenid;
      }
    """))
  }

  val txType: SolType by lazy {
    SolStruct(psiFactory.createStruct("""
      struct ${internalise("Tx")} {
          uint gasprice;
          address origin;
      }
    """))
  }

  val addressType: SolContract by lazy {
    SolContract(psiFactory.createContract("""
      contract ${internalise("Address")} {
          function transfer(uint value);
          function send(uint value) returns (bool);
          function transferToken(uint256 tokenValue, trcToken tokenId);
          function tokenBalance(trcToken tokenId) returns (uint256);
      }
    """))
  }

  val arrayType: SolContract by lazy {
    SolContract(psiFactory.createContract("""
      contract ${internalise("Array")} {
          function push(uint value);
      }
    """))
  }

  val blockType: SolType by lazy {
    BuiltinType(internalise("Block"), listOf(
      BuiltinCallable(listOf(), SolAddress, "coinbase", null, Usage.VARIABLE),
      BuiltinCallable(listOf(), UINT_256, "number", null, Usage.VARIABLE),
      BuiltinCallable(listOf(), UINT_256, "timestamp", null, Usage.VARIABLE),
      BuiltinCallable(listOf("blockNumber" to UINT_256), SolFixedBytes(32), "blockhash", null, Usage.VARIABLE)
    ))
  }

  val globalType: SolContract by lazy {
    SolContract(psiFactory.createContract("""
      contract Global {
          $blockType block;
          $msgType msg;
          $txType tx;
          uint now;

          function assert(bool condition) private {}
          function require(bool condition) private {}
          function require(bool condition, string message) private {}
          function revert() private {}
          function revert(string) {}
          function keccak256() returns (bytes32) private {}
          function sha3() returns (bytes32) private {}
          function sha256() returns (bytes32) private {}
          function ripemd160() returns (bytes20) private {}
          function ecrecover(bytes32 hash, uint8 v, bytes32 r, bytes32 s) returns (address) private {}
          function addmod(uint x, uint y, uint k) returns (uint) private {}
          function mulmod(uint x, uint y, uint k) returns (uint) private returns (uint) {}
          function selfdestruct(address recipient) private {};
      }
    """))
  }
}
