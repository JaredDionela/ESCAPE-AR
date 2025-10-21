import { Box, Paper, Typography } from '@mui/material'
import YouTube from 'react-youtube'

interface YouTubePreviewProps {
  videoId: string
}

export function YouTubePreview({ videoId }: YouTubePreviewProps) {
  if (!videoId) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center', backgroundColor: 'grey.100' }}>
        <Typography color="text.secondary">
          Enter a YouTube video ID to see preview
        </Typography>
      </Paper>
    )
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        📺 Video Preview
      </Typography>
      <Paper sx={{ overflow: 'hidden' }}>
        <YouTube
          videoId={videoId}
          opts={{
            width: '100%',
            height: '315',
            playerVars: {
              autoplay: 0,
            },
          }}
        />
      </Paper>
    </Box>
  )
}
