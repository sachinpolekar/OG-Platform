/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;
import static org.junit.Assert.*;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

public class CachingFunctionRepositoryCompilerTest {
  
  private static class MockFunction extends AbstractFunction {
    
    private final AtomicInteger _compileCount = new AtomicInteger ();
    private final Long _validBefore;
    private final Long _validAfter;
    
    private MockFunction (final String name, final Long validBefore, final Long validAfter) {
      setUniqueIdentifier(name);
      _validBefore = validBefore;
      _validAfter = validAfter;
    }

    @Override
    public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstantProvider) {
      _compileCount.incrementAndGet();
      final Instant atInstant = Instant.of (atInstantProvider);
      final AbstractFunction.AbstractCompiledFunction compiled = new AbstractFunction.AbstractCompiledFunction() {
        
        @Override
        public ComputationTargetType getTargetType() {
          return null;
        }
        
        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return null;
        }
        
        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
          return null;
        }
        
        @Override
        public FunctionInvoker getFunctionInvoker() {
          return null;
        }
        
        @Override
        public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
          return false;
        }
        
      };
      if (_validBefore != null) {
        compiled.setEarliestInvocationTime(atInstant.minusMillis(_validBefore));
      }
      if (_validAfter != null) {
        compiled.setLatestInvocationTime(atInstant.plusMillis(_validAfter));
      }
      return compiled;
    }

    @Override
    public String getShortName() {
      return getUniqueIdentifier ();
    }
    
  }
  
  @Test
  public void testCompileFunction () {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository ();
    final MockFunction alwaysValid = new MockFunction ("always valid", null, null);
    final MockFunction validUntil = new MockFunction ("valid until", null, 30L);
    final MockFunction validFrom = new MockFunction ("valid from", 30L, null);
    final MockFunction validWithin = new MockFunction ("valid within", 30L, 30L);
    functions.addFunction (alwaysValid);
    functions.addFunction (validUntil);
    functions.addFunction (validFrom);
    functions.addFunction (validWithin);
    final CachingFunctionRepositoryCompiler compiler = new CachingFunctionRepositoryCompiler();
    final CompiledFunctionService context = new CompiledFunctionService (functions, compiler, new FunctionCompilationContext ());
    final Instant timestamp = Instant.nowSystemClock();

    // Everything compiled once
    final CompiledFunctionRepository compiledFunctionsNow = context.compileFunctionRepository(timestamp);
    assertSame (alwaysValid, compiledFunctionsNow.getDefinition(alwaysValid.getUniqueIdentifier ()).getFunctionDefinition ());
    assertSame (validUntil, compiledFunctionsNow.getDefinition(validUntil.getUniqueIdentifier ()).getFunctionDefinition ());
    assertSame (validFrom, compiledFunctionsNow.getDefinition(validFrom.getUniqueIdentifier ()).getFunctionDefinition ());
    assertSame (validWithin, compiledFunctionsNow.getDefinition(validWithin.getUniqueIdentifier ()).getFunctionDefinition ());
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (1, validUntil._compileCount.get ());
    assertEquals (1, validFrom._compileCount.get ());
    assertEquals (1, validWithin._compileCount.get ());
    
    // All previously compiled ones still valid, so should use the "previous" cache
    final CompiledFunctionRepository compiledFunctionsAheadWithin = context.compileFunctionRepository (timestamp.plusMillis(29L));
    assertSame (compiledFunctionsNow, compiledFunctionsAheadWithin);
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (1, validUntil._compileCount.get ());
    assertEquals (1, validFrom._compileCount.get ());
    assertEquals (1, validWithin._compileCount.get ());
    
    // All previously compiled ones still valid, so should use the "previous" cache
    final CompiledFunctionRepository compiledFunctionsAheadLimit = context.compileFunctionRepository (timestamp.plusMillis(30L));
    assertSame (compiledFunctionsNow, compiledFunctionsAheadLimit);
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (1, validUntil._compileCount.get ());
    assertEquals (1, validFrom._compileCount.get ());
    assertEquals (1, validWithin._compileCount.get ());
    
    // Some functions to be recompiled, others from the "previous" cache
    final CompiledFunctionRepository compiledFunctionsAheadBeyond = context.compileFunctionRepository (timestamp.plusMillis(31L));
    assertNotSame (compiledFunctionsNow, compiledFunctionsAheadBeyond);
    assertSame (compiledFunctionsNow.getDefinition (alwaysValid.getUniqueIdentifier ()), compiledFunctionsAheadBeyond.getDefinition (alwaysValid.getUniqueIdentifier ()));
    assertNotSame (compiledFunctionsNow.getDefinition (validUntil.getUniqueIdentifier()), compiledFunctionsAheadBeyond.getDefinition(validUntil.getUniqueIdentifier()));
    assertSame (compiledFunctionsNow.getDefinition (validFrom.getUniqueIdentifier ()), compiledFunctionsAheadBeyond.getDefinition (validFrom.getUniqueIdentifier ()));
    assertNotSame (compiledFunctionsNow.getDefinition (validWithin.getUniqueIdentifier()), compiledFunctionsAheadBeyond.getDefinition(validWithin.getUniqueIdentifier()));
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (2, validUntil._compileCount.get ());
    assertEquals (1, validFrom._compileCount.get ());
    assertEquals (2, validWithin._compileCount.get ());
    
    // All previously compiled functions, so should use the "ahead" cache
    final CompiledFunctionRepository compiledFunctionsBeforeWithin = context.compileFunctionRepository(timestamp.minusMillis(30L));
    assertSame (compiledFunctionsNow, compiledFunctionsBeforeWithin);
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (2, validUntil._compileCount.get ());
    assertEquals (1, validFrom._compileCount.get ());
    assertEquals (2, validWithin._compileCount.get ());
    
    // Some functions to be recompiled, others from the "ahead" cache
    final CompiledFunctionRepository compiledFunctionsBeforeBeyond = context.compileFunctionRepository (timestamp.minusMillis(31L));
    assertNotSame (compiledFunctionsNow, compiledFunctionsBeforeBeyond);
    assertSame (compiledFunctionsNow.getDefinition (alwaysValid.getUniqueIdentifier ()), compiledFunctionsBeforeBeyond.getDefinition (alwaysValid.getUniqueIdentifier ()));
    assertSame (compiledFunctionsNow.getDefinition (validUntil.getUniqueIdentifier()), compiledFunctionsBeforeBeyond.getDefinition(validUntil.getUniqueIdentifier()));
    assertNotSame (compiledFunctionsNow.getDefinition (validFrom.getUniqueIdentifier ()), compiledFunctionsBeforeBeyond.getDefinition (validFrom.getUniqueIdentifier ()));
    assertNotSame (compiledFunctionsNow.getDefinition (validWithin.getUniqueIdentifier()), compiledFunctionsBeforeBeyond.getDefinition(validWithin.getUniqueIdentifier()));
    assertEquals (1, alwaysValid._compileCount.get ());
    assertEquals (2, validUntil._compileCount.get ());
    assertEquals (2, validFrom._compileCount.get ());
    assertEquals (3, validWithin._compileCount.get ());
    
  }
  
}