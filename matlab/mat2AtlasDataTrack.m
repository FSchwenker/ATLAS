function [ inData ] = mat2atlasDataTrack( data )
%ATLASDATATRACK2STRUCT Summary of this function goes here
%   Detailed explanation goes here

numOfSamples = size(data,2);

inData = cell(1,numOfSamples);
for s=1:numOfSamples
   inData{1,s}=data(:,s)';
end

end
