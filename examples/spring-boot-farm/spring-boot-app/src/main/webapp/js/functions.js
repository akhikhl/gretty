jQuery.noConflict();

jQuery(function($) {
  $('#sendRequest1').click(function() {
    $('#result1').addClass('hide');
    $.ajax({ type: 'POST', url: '/spring-boot-webservice/mycontroller/getdate', dataType: 'json' }).done(function(data) {
      $('#result1').removeClass('hide bg-danger').addClass('bg-success').text('Got from server: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result1').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
  $('#sendRequest2').click(function() {
    $('#result2').addClass('hide');
    $.ajax({ type: 'POST', url: '/jee-webservice/myservlet/getdate', dataType: 'json' }).done(function(data) {
      $('#result2').removeClass('hide bg-danger').addClass('bg-success').text('Got from server: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result2').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
});
