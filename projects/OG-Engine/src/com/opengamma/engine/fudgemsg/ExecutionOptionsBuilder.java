/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.EnumSet;

import org.apache.commons.lang.BooleanUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;

/**
 * Fudge message builder for {@link ExecutionOptions}
 */
@FudgeBuilderFor(ExecutionOptions.class)
public class ExecutionOptionsBuilder implements FudgeBuilder<ExecutionOptions> {

  private static final String EXECUTION_SEQUENCE_FIELD = "executionSequence";
  
  private static final String AWAIT_MARKET_DATA_FIELD = "awaitMarketData";
  private static final String TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD = "liveDataTriggerEnabled";
  private static final String TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD = "timeElapsedTriggerEnabled";
  private static final String RUN_AS_FAST_AS_POSSIBLE_FIELD = "runAsFastAsPossible";
  private static final String COMPILE_ONLY_FIELD = "compileOnly";
  
  private static final String MAX_SUCCESSIVE_DELTA_CYCLES_FIELD = "maxSuccessiveDeltaCycles";
  private static final String DEFAULT_EXECUTION_OPTIONS_FIELD = "defaultExecutionOptions";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ExecutionOptions object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessageWithClassHeaders(msg, EXECUTION_SEQUENCE_FIELD, null, object.getExecutionSequence());
    msg.add(AWAIT_MARKET_DATA_FIELD, object.getFlags().contains(ViewExecutionFlags.AWAIT_MARKET_DATA));
    msg.add(TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD, object.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED));
    msg.add(TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD, object.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED));
    msg.add(RUN_AS_FAST_AS_POSSIBLE_FIELD, object.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    msg.add(COMPILE_ONLY_FIELD, object.getFlags().contains(ViewExecutionFlags.COMPILE_ONLY));
    if (object.getMaxSuccessiveDeltaCycles() != null) {
      msg.add(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD, object.getMaxSuccessiveDeltaCycles());
    }
    context.addToMessage(msg, DEFAULT_EXECUTION_OPTIONS_FIELD, null, object.getDefaultExecutionOptions());
    return msg;
  }

  @Override
  public ExecutionOptions buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ViewCycleExecutionSequence executionSequence = context.fudgeMsgToObject(ViewCycleExecutionSequence.class, message.getMessage(EXECUTION_SEQUENCE_FIELD));
    EnumSet<ViewExecutionFlags> flags = EnumSet.noneOf(ViewExecutionFlags.class);
    if (BooleanUtils.isTrue(message.getBoolean(AWAIT_MARKET_DATA_FIELD))) {
      flags.add(ViewExecutionFlags.AWAIT_MARKET_DATA);
    }
    if (BooleanUtils.isTrue(message.getBoolean(TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD))) {
      flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED);
    }
    if (BooleanUtils.isTrue(message.getBoolean(TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD))) {
      flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED);
    }
    if (BooleanUtils.isTrue(message.getBoolean(RUN_AS_FAST_AS_POSSIBLE_FIELD))) {
      flags.add(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE);
    }
    if (BooleanUtils.isTrue(message.getBoolean(COMPILE_ONLY_FIELD))) {
      flags.add(ViewExecutionFlags.COMPILE_ONLY);
    }
    Integer maxSuccessiveDeltaCycles = null;
    if (message.hasField(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD)) {
      maxSuccessiveDeltaCycles = message.getInt(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD);
    }
    FudgeField defaultExecutionOptionsField = message.getByName(DEFAULT_EXECUTION_OPTIONS_FIELD);
    ViewCycleExecutionOptions defaultExecutionOptions = defaultExecutionOptionsField != null ?
        context.fieldValueToObject(ViewCycleExecutionOptions.class, defaultExecutionOptionsField) : null;
    return new ExecutionOptions(executionSequence, flags, maxSuccessiveDeltaCycles, defaultExecutionOptions);
  }

}
