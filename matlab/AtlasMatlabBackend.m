function [ outTracks, outAudio, outVideo, outLabels, outScalars, outVectors] = AtlasMatlabBackend( project, tracks, audio, video, labels, scalars, vectors)

    save();
[project, tracks, audio, video, labels, scalars, vectors] = atlasOut2Matlab(project, tracks, audio, video, labels, scalars, vectors);

    %save();
    
   
    %clear all
    %load('changed.mat');
    
    [outTracks, outAudio, outVideo, outLabels, outScalars, outVectors] = matlab2AtlasIn(tracks, audio, video, labels, scalars, vectors);

end

