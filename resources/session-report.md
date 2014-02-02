Video session
=============

Session id: {{session-id}}
User agent: {{user-agent}}
Max duration: {{duration}}

### Segments
{% for segment in segments %}
  - Event: {{segment.0}}, {{segment.1}}
{% endfor %}

### Errors and warnings
{% for message in validation-data %}
- [{{message.code}}]: {{message.message}}

{% endfor %}




