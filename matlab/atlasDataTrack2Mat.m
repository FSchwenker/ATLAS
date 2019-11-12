function [ outData ] = atlasDataTrack2Mat( inData )
%ATLASDATATRACK2STRUCT Summary of this function goes here
%   Detailed explanation goes here

numOfSamples = size(inData,2);
numOfDimensions = size(inData{1},2);
outData = zeros(numOfDimensions, numOfSamples);

for s=1:numOfSamples
    for d=1:numOfDimensions
        outData(d,s)=inData{s}(d);
    end
end

end

