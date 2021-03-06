/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.lang.reflect.Array;
import java.util.Map;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Converts arrays from X[] to Y[]
 */
public class ArrayTypeConverter implements TypeConverter {

  private static final JavaTypeInfo<Object> OBJECT = JavaTypeInfo.builder(Object.class).get();
  private static final Map<JavaTypeInfo<?>, Integer> FROM_OBJECT = TypeMap.of(ZERO_LOSS, OBJECT);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return targetType.isArray();
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (!value.getClass().isArray()) {
      conversionContext.setFail();
      return;
    }
    final JavaTypeInfo<?> element = type.getArrayElementType();
    final int length = Array.getLength(value);
    final Object result = Array.newInstance(element.getRawClass(), length);
    for (int i = 0; i < length; i++) {
      final Object sourceValue = Array.get(value, i);
      conversionContext.convertValue(sourceValue, element);
      if (conversionContext.isFailed()) {
        conversionContext.setFail();
        return;
      }
      Array.set(result, i, conversionContext.getResult());
    }
    conversionContext.setResult(result);
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    return FROM_OBJECT;
  }

}
