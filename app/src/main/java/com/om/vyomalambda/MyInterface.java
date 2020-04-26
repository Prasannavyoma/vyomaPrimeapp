package com.om.vyomalambda;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
public interface MyInterface {

    /**
     * Invoke the Lambda function "AndroidBackendLambdaFunction".
     * The function name is the method name.
     */
    @LambdaFunction
    com.om.vyomalambda.ResponseClass AndroidBackendLambdaFunction(com.om.vyomalambda.RequestClass request);

}