/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.saga.omega.transaction.spring;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils.MethodCallback;

import io.servicecomb.saga.omega.transaction.annotations.Compensable;

class CompensableMethodCheckingCallback implements MethodCallback {
  private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Object bean;

  CompensableMethodCheckingCallback(Object bean) {
    this.bean = bean;
  }

  @Override
  public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
    if (!method.isAnnotationPresent(Compensable.class)) {
      return;
    }

    String compensationMethod = method.getAnnotation(Compensable.class).compensationMethod();

    try {
      bean.getClass().getDeclaredMethod(compensationMethod, method.getParameterTypes());
      LOG.debug("Found compensation method [{}] in {}", compensationMethod, bean.getClass().getCanonicalName());
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "No such compensation method [" + compensationMethod + "] found in " + bean.getClass().getCanonicalName(),
          e);
    }
  }
}
