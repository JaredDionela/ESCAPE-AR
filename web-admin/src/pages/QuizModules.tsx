import { Box, Paper, Typography, Grid, Card, CardContent, CardActionArea, Chip } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getAllQuizQuestions } from '../lib/api/quiz';
import { QuizQuestion } from '../types/database.types';
import {
  Science as DecantationIcon,
  Favorite as OrganIcon,
  Build as MachinesIcon,
  Public as SolarIcon,
} from '@mui/icons-material';

const MODULES = [
  {
    id: 'decantation',
    title: 'Decantation',
    description: 'Separation of mixtures and solutions',
    color: '#2196F3',
    icon: DecantationIcon,
  },
  {
    id: 'organ_system',
    title: 'Organ System',
    description: 'Human body systems and functions',
    color: '#4CAF50',
    icon: OrganIcon,
  },
  {
    id: 'simple_machines',
    title: 'Simple Machines',
    description: 'Levers, pulleys, and mechanical advantage',
    color: '#FF9800',
    icon: MachinesIcon,
  },
  {
    id: 'solar_system',
    title: 'Solar System',
    description: 'Planets, stars, and space',
    color: '#9C27B0',
    icon: SolarIcon,
  },
];

export default function QuizModules() {
  const navigate = useNavigate();
  const { data: allQuestions = [] } = useQuery<QuizQuestion[]>({
    queryKey: ['quiz-questions'],
    queryFn: getAllQuizQuestions,
  });

  const getQuestionCount = (moduleId: string) => {
    return allQuestions.filter((q) => q.module_id === moduleId).length;
  };

  return (
    <Box>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          📝 Quiz Management
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Select a module to manage its quiz questions
        </Typography>
      </Paper>

      <Grid container spacing={3}>
        {MODULES.map((module) => {
          const Icon = module.icon;
          const questionCount = getQuestionCount(module.id);

          return (
            <Grid item xs={12} sm={6} md={3} key={module.id}>
              <Card
                sx={{
                  height: '100%',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-8px)',
                    boxShadow: 6,
                  },
                }}
              >
                <CardActionArea
                  onClick={() => navigate(`/quiz/${module.id}`)}
                  sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'stretch',
                    justifyContent: 'flex-start',
                  }}
                >
                  <Box
                    sx={{
                      background: `linear-gradient(135deg, ${module.color} 0%, ${module.color}dd 100%)`,
                      p: 3,
                      textAlign: 'center',
                      color: 'white',
                    }}
                  >
                    <Icon sx={{ fontSize: 64, mb: 1 }} />
                    <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
                      {module.title}
                    </Typography>
                  </Box>
                  <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {module.description}
                    </Typography>
                    <Chip
                      label={`${questionCount} Question${questionCount !== 1 ? 's' : ''}`}
                      color={questionCount > 0 ? 'success' : 'default'}
                      size="small"
                      sx={{ fontWeight: 'bold' }}
                    />
                  </CardContent>
                </CardActionArea>
              </Card>
            </Grid>
          );
        })}
      </Grid>
    </Box>
  );
}
