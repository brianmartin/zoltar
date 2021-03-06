/*-
 * -\-\-
 * apollo-service-example
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.zoltar.examples.apollo;

import com.spotify.featran.FeatureSpec;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.tf.TensorFlowLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.tensorflow.example.Example;

/**
 * Iris prediction meat and potatoes.
 */
public final class IrisPredictor {

  /**
   * Configure Iris prediction, should be called at the service startup/configuration stage.
   */

  public static Predictor<Iris, Long> create(final ModelConfig modelConfig,
                                             final PredictorMetrics metrics) throws IOException {

    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final String settings = new String(Files.readAllBytes(Paths.get(modelConfig.settingsUri())));
    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(irisFeatureSpec, settings);
    final TensorFlowLoader modelLoader = TensorFlowLoader
        .create(modelConfig.modelUri().toString(), modelConfig.modelLoaderExecutor());

    final String[] ops = new String[]{"linear/head/predictions/class_ids"};
    return Predictors.tensorFlow(
        modelLoader,
        extractFn,
        tensors -> tensors.get(ops[0]).longValue()[0],
        ops,
        metrics);
  }

}
