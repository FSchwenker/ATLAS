function [ outProject, outTracks, outAudio, outVideo, outLabels, outScalars, outVectors ] = atlasOut2Matlab( project, tracks, audio, video, labels, scalars, vectors )
%ATLASOUT2MATLAB Summary of this function goes here
%   Detailed explanation goes here

outTracks = [];
outLabels = [];
outScalars = [];
outVectors = [];
outAudio = audio;
outVideo = video;

outProject = atlasProjectXML2Struct(project(1:end));

for i=1:5
    outTracks{i} = strsplit(tracks{i},';');
end

for i=1:size(labels,2)
    outLabels{i} = atlasLabelTrackXML2Struct(labels{i});
end

for i=1:size(scalars,2)
    outScalars{i} = atlasDataTrack2Mat(scalars{i});
end

for i=1:size(vectors,2)
    outVectors{i} = atlasDataTrack2Mat(vectors{i});
end

end

