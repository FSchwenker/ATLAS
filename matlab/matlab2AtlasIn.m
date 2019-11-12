function [inTracks, inAudio, inVideo, inLabels, inScalars, inVectors ] = matlab2AtlasIn(tracks, audio, video, labels, scalars, vectors )
%ATLASOUT2MATLAB Summary of this function goes here
%   Detailed explanation goes here


inTracks = [];
inLabels = [];
inScalars = [];
inVectors = [];

for i=1:5
    
    inTracks{i} = strjoin(tracks{i},';');
end

inAudio = audio;
inVideo = video;

for i=1:size(labels,2)
    l = struct2AtlasLabelTrackXML(labels{i});
    
    inLabels{i} = struct2AtlasLabelTrackXML(labels{i});
end

for i=1:size(scalars,2)
    inScalars{i} = mat2AtlasDataTrack(scalars{i});
end

for i=1:size(vectors,2)
    inVectors{i} = mat2AtlasDataTrack(vectors{i});
end

end

